package com.mylogisticcba.core.service.impl;

import com.mylogisticcba.core.dto.req.CustomerCreationRequest;
import com.mylogisticcba.core.dto.req.OrderCreationRequest;
import com.mylogisticcba.core.dto.req.OrderItemRequest;
import com.mylogisticcba.core.dto.response.OrderCreatedResponse;
import com.mylogisticcba.core.entity.Customer;
import com.mylogisticcba.core.entity.Order;
import com.mylogisticcba.core.entity.OrderItem;
import com.mylogisticcba.core.entity.Product;
import com.mylogisticcba.core.exceptions.OrderServiceException;
import com.mylogisticcba.core.repository.orders.CustomerRepository;
import com.mylogisticcba.core.repository.orders.OrderRepository;
import com.mylogisticcba.core.repository.orders.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService implements com.mylogisticcba.core.service.OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public OrderCreatedResponse createOrder(OrderCreationRequest orderCreationRequest) {
        // Determinar/crear customer
        Customer customer = null;
        UUID customerId = orderCreationRequest.getCustomerId();

        if (customerId != null) {
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isPresent()) {
                customer = customerOpt.get();
            }
        }

        // si no se encontró por id (o no se envió), intentar crear con customerCreationRequest
        if (customer == null) {
            customer = createNewCustomer(orderCreationRequest);
        }

        // si después de todo sigue sin customer, error controlado
        if (customer == null) {
            throw new OrderServiceException("CustomerId no provisto y customerCreationRequest ausente o inválido", HttpStatus.BAD_REQUEST);
        }

        // Construyo la orden
        Order order = new Order();
        order.setCustomer(customer);
        order.setTenantId(customer.getTenantId());
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString());
        order.setOrderDate(LocalDateTime.now());
        order.setNotes(orderCreationRequest.getNotes());

        List<OrderItem> items = new ArrayList<>();

        // Procesar ítems
        for (OrderItemRequest itemReq : orderCreationRequest.getItems()) {
            Optional<Product> productOpt = productRepository.findById(itemReq.getProductId());
            if (productOpt.isEmpty()) {
                throw new OrderServiceException("Producto no encontrado: " + itemReq.getProductId(), HttpStatus.BAD_REQUEST);
            }
            Product product = productOpt.get();


            // Crear OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .notes(null)
                    .build();

            // Asignar relación
            orderItem.setOrder(order);
            // Calcular subtotal
            BigDecimal subtotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            orderItem.setSubtotal(subtotal);

            // Reducir stock del producto
            productRepository.save(product);

            items.add(orderItem);
        }

        order.setOrderItems(items);
        order.calculateTotalAmount();
        order.setStatus(Order.OrderStatus.PENDING);

        Order saved = orderRepository.save(order);

        // Construir respuesta
        OrderCreatedResponse resp = new OrderCreatedResponse();
        resp.setOrderId(saved.getId());
        resp.setOrderNumber(saved.getOrderNumber());
        resp.setTotalAmount(saved.getTotalAmount());
        resp.setStatus(saved.getStatus().name());
        resp.setCreatedAt(saved.getCreatedAt() == null ? Instant.now() : saved.getCreatedAt());

        return resp;
    }

    // Helper to create a new customer from the OrderCreationRequest.customerCreationRequest
    private Customer createNewCustomer(OrderCreationRequest orderCreationRequest) {
        CustomerCreationRequest cReq = orderCreationRequest.getCustomerCreationRequest();
        if (cReq == null) {
            return null; // caller will decide to throw
        }

        //algoritmo para obtener latitud y longitud desde la direccion podria ir aqui
        //getLatLongFromAddress(cReq);
        double latitude = 0.0;
        double longitude = 0.0;

        Customer newCustomer = Customer.builder()
                .tenantId(cReq.getTenantId())
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
                .build();

        try {
            if (cReq.getType() != null) {
                newCustomer.setType(Customer.CustomerType.valueOf(cReq.getType().toUpperCase()));
            }
        }
        catch (Exception ex) {

        }

        return customerRepository.save(newCustomer);
    }

}
