package com.mylogisticcba.core.service.impl;

import com.mylogisticcba.core.dto.req.CustomerCreationRequest;
import com.mylogisticcba.core.dto.req.OrderCreationRequest;
import com.mylogisticcba.core.dto.req.OrderItemRequest;
import com.mylogisticcba.core.dto.req.OrderUpdateRequest;
import com.mylogisticcba.core.dto.response.OrderCreatedResponse;
import com.mylogisticcba.core.dto.response.OrderItemResponse;
import com.mylogisticcba.core.dto.response.OrderResponse;
import com.mylogisticcba.core.entity.Customer;
import com.mylogisticcba.core.entity.Order;
import com.mylogisticcba.core.entity.OrderItem;
import com.mylogisticcba.core.entity.Product;
import com.mylogisticcba.core.entity.Zone;
import com.mylogisticcba.core.exceptions.OrderServiceException;
import com.mylogisticcba.core.repository.orders.CustomerRepository;
import com.mylogisticcba.core.repository.orders.OrderRepository;
import com.mylogisticcba.core.repository.orders.ProductRepository;
import com.mylogisticcba.core.repository.orders.ZoneRepository;
import com.mylogisticcba.core.service.rest.LatLng;
import com.mylogisticcba.core.service.rest.NominatimGeoService;
import com.mylogisticcba.core.service.rest.StructuredAddress;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService implements com.mylogisticcba.core.service.OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ZoneRepository zoneRepository;
    private final NominatimGeoService nominatimGeoService;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        ZoneRepository zoneRepository,
                        NominatimGeoService nominatimGeoService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.zoneRepository = zoneRepository;
        this.nominatimGeoService = nominatimGeoService;
    }

    @Override
    public OrderCreatedResponse createOrder(OrderCreationRequest orderCreationRequest) {
        // 1. GEOCODIFICAR PRIMERO (fuera de transacción)
        LatLng coordinates = geocodeIfNeeded(orderCreationRequest);

        // 2. CREAR ORDEN EN TRANSACCIÓN (con coordenadas ya obtenidas)
        return createOrderTransactional(orderCreationRequest, coordinates);
    }

    /**
     * Geocodifica la dirección del customer SI es necesario.
     * Se ejecuta FUERA de la transacción para no bloquear el thread transaccional.
     */
    private LatLng geocodeIfNeeded(OrderCreationRequest orderCreationRequest) {
        // Solo geocodificar si vamos a crear un nuevo customer
        if (orderCreationRequest.getCustomerId() != null) {
            return null; // Usar customer existente, no geocodificar
        }

        CustomerCreationRequest cReq = orderCreationRequest.getCustomerCreationRequest();
        if (cReq == null || cReq.getAddress() == null || cReq.getAddress().isBlank()) {
            return null; // No hay dirección para geocodificar
        }

        try {
            StructuredAddress address = new StructuredAddress()
                    .street(cReq.getAddress())
                    .city(cReq.getCity())
                    .state(cReq.getState())
                    .country(cReq.getCountry())
                    .postalCode(cReq.getPostalCode());

            LatLng latLng = nominatimGeoService.getLatLngStructured(address);

            if (latLng == null) {
                String fullAddress = String.format("%s, %s, %s, %s, %s",
                        cReq.getAddress(),
                        cReq.getCity(),
                        cReq.getState(),
                        cReq.getCountry(),
                        cReq.getPostalCode());
                throw new OrderServiceException(
                        String.format("La dirección '%s' no pudo ser validada. Por favor, verifica que todos los datos sean correctos y correspondan a una ubicación real.", fullAddress),
                        HttpStatus.BAD_REQUEST
                );
            }

            log.info("Geocodificación exitosa: lat={}, lon={}", latLng.getLat(), latLng.getLon());
            return latLng;

        } catch (OrderServiceException ex) {
            // Re-lanzar excepciones de OrderService
            throw ex;
        } catch (Exception ex) {
            log.error("Error geocodificando dirección: {}", ex.getMessage());
            String fullAddress = String.format("%s, %s, %s, %s, %s",
                    cReq.getAddress(),
                    cReq.getCity(),
                    cReq.getState(),
                    cReq.getCountry(),
                    cReq.getPostalCode());
            throw new OrderServiceException(
                    String.format("Error al validar la dirección '%s'. %s", fullAddress, ex.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * Crea la orden dentro de una transacción.
     * Recibe las coordenadas ya calculadas para evitar llamadas HTTP dentro de la transacción.
     */
    @Transactional
    protected OrderCreatedResponse createOrderTransactional(
            OrderCreationRequest orderCreationRequest,
            LatLng coordinates) {

        // 1. Determinar/crear customer
        Customer customer = resolveCustomer(orderCreationRequest, coordinates);

        if (customer == null) {
            throw new OrderServiceException(
                    "Debes proporcionar un ID de cliente existente o los datos completos para crear un nuevo cliente.",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 2. Construir la orden
        Order order = new Order();
        order.setCustomer(customer);
        order.setTenantId(customer.getTenantId());
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 13));
        order.setOrderProgramDate(LocalDateTime.now());
        order.setNotes(orderCreationRequest.getNotes());
        order.setStatus(Order.OrderStatus.PENDING);

        // 3. Procesar items
        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : orderCreationRequest.getItems()) {
            Optional<Product> productOpt = productRepository.findById(itemReq.getProductId());

            if (productOpt.isEmpty()) {
                throw new OrderServiceException(
                        String.format("No se encontró el producto con ID '%s'. Verifica que el producto exista en tu catálogo.", itemReq.getProductId()),
                        HttpStatus.BAD_REQUEST
                );
            }

            Product product = productOpt.get();

            // Crear OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .notes(null)
                    .build();

            // Calcular subtotal
            BigDecimal subtotal = itemReq.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            orderItem.setSubtotal(subtotal);

            // Asignar relación bidireccional
            orderItem.setOrder(order);

            items.add(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }

        order.setOrderItems(items);
        order.setTotalAmount(totalAmount);

        // 4. Guardar orden (cascade guardará los items)
        Order saved = orderRepository.save(order);

        // 5. Construir respuesta
        OrderCreatedResponse resp = new OrderCreatedResponse();
        resp.setOrderId(saved.getId());
        resp.setOrderNumber(saved.getOrderNumber());

        return resp;
    }

    /**
     * Resuelve el customer: busca uno existente o crea uno nuevo.
     */
    private Customer resolveCustomer(OrderCreationRequest orderCreationRequest, LatLng coordinates) {
        UUID customerId = orderCreationRequest.getCustomerId();

        // Intentar buscar customer existente por ID
        if (customerId != null) {
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isPresent()) {
                return customerOpt.get();
            }
        }

        // Si no se encontró o no se proveyó ID, crear nuevo customer
        return createNewCustomer(orderCreationRequest, coordinates);
    }

    /**
     * Crea un nuevo customer con las coordenadas ya calculadas.
     */
    private Customer createNewCustomer(OrderCreationRequest orderCreationRequest, LatLng coordinates) {
        CustomerCreationRequest cReq = orderCreationRequest.getCustomerCreationRequest();

        if (cReq == null) {
            return null; // El caller decidirá si lanzar excepción
        }

        // Usar coordenadas si están disponibles
        double latitude = 0.0;
        double longitude = 0.0;

        if (coordinates != null) {
            latitude = coordinates.getLat();
            longitude = coordinates.getLon();
        }

        UUID tenantId = TenantContextHolder.getTenant();

        // Buscar zona si se proporciona zoneId
        Zone zone = null;
        if (cReq.getZoneId() != null && !cReq.getZoneId().isBlank()) {
            try {
                UUID zoneId = UUID.fromString(cReq.getZoneId());
                zone = zoneRepository.findByIdAndTenantId(zoneId, tenantId)
                        .orElseThrow(() -> new OrderServiceException(
                                String.format("No se encontró la zona con ID '%s'. Por favor, verifica que la zona exista y esté asignada a tu organización.", cReq.getZoneId()),
                                HttpStatus.BAD_REQUEST
                        ));
            } catch (IllegalArgumentException ex) {
                throw new OrderServiceException(
                        String.format("El ID de zona '%s' no tiene un formato válido. Debe ser un UUID válido.", cReq.getZoneId()),
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        Customer newCustomer = Customer.builder()
                .tenantId(tenantId)
                .name(cReq.getName())
                .email(cReq.getEmail())
                .phoneNumber(cReq.getPhoneNumber())
                .address(cReq.getAddress())
                .latitude(latitude)
                .longitude(longitude)
                .postalCode(cReq.getPostalCode())
                .city(cReq.getCity())
                .state(cReq.getState())
                .country(cReq.getCountry())
                .notes(cReq.getNotes())
                .doorbell(cReq.getDoorbell())
                .zone(zone)
                .isActive(cReq.getIsActive() != null ? cReq.getIsActive() : true)
                .build();

        // Intentar parsear el tipo de customer
        try {
            if (cReq.getType() != null && !cReq.getType().isBlank()) {
                newCustomer.setType(Customer.CustomerType.valueOf(cReq.getType().toUpperCase()));
            }
        } catch (IllegalArgumentException ex) {
            log.warn("Tipo de customer inválido: {}. Usando valor por defecto.", cReq.getType());
            // Ignorar tipo inválido; no bloquear la creación del customer
        }

        return customerRepository.save(newCustomer);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        UUID tenantId = TenantContextHolder.getTenant();
        return orderRepository.findAll().stream()
                .filter(order -> order.getTenantId().equals(tenantId))
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderById(UUID id) {
        UUID tenantId = TenantContextHolder.getTenant();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

        if (!order.getTenantId().equals(tenantId)) {
            throw new EntityNotFoundException("Order not found in this tenant");
        }

        return toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(UUID id, OrderUpdateRequest request) {
        UUID tenantId = TenantContextHolder.getTenant();

        // Buscar la orden
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderServiceException(
                        "Order not found with id: " + id,
                        HttpStatus.NOT_FOUND
                ));

        // Verificar que pertenezca al tenant
        if (!order.getTenantId().equals(tenantId)) {
            throw new OrderServiceException(
                    "Order not found in this tenant",
                    HttpStatus.FORBIDDEN
            );
        }

        // Actualizar el status si viene en el request
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                Order.OrderStatus newStatus = Order.OrderStatus.valueOf(request.getStatus().toUpperCase());
                order.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new OrderServiceException(
                        "Invalid status: " + request.getStatus(),
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        // Actualizar notas si vienen en el request
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        // Guardar cambios
        Order updated = orderRepository.save(order);

        return toOrderResponse(updated);
    }

    private OrderResponse toOrderResponse(Order order) {
        Customer customer = order.getCustomer();

        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .tenantId(order.getTenantId())
                .customerId(customer.getId())
                .customerName(customer.getName())
                .customerAddress(customer.getAddress())
                .customerCity(customer.getCity())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}