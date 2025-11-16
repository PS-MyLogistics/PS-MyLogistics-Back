package com.mylogisticcba.core.service.impl;

import com.mylogisticcba.core.dto.req.CustomerCreationRequest;
import com.mylogisticcba.core.dto.response.CustomerResponse;
import com.mylogisticcba.core.entity.Customer;
import com.mylogisticcba.core.entity.Zone;
import com.mylogisticcba.core.exceptions.OrderServiceException;
import com.mylogisticcba.core.repository.orders.CustomerRepository;
import com.mylogisticcba.core.repository.orders.ZoneRepository;
import com.mylogisticcba.core.service.CustomerService;
import com.mylogisticcba.core.service.rest.LatLng;
import com.mylogisticcba.core.service.rest.NominatimGeoService;
import com.mylogisticcba.core.service.rest.StructuredAddress;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final ZoneRepository zoneRepository;
    private final NominatimGeoService geoService;

    public CustomerServiceImpl(CustomerRepository customerRepository, ZoneRepository zoneRepository, NominatimGeoService geoService) {
        this.customerRepository = customerRepository;
        this.zoneRepository = zoneRepository;
        this.geoService = geoService;
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        UUID tenantId = TenantContextHolder.getTenant();
        return customerRepository.findAll().stream()
                .filter(customer -> customer.getTenantId().equals(tenantId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerResponse getCustomerById(UUID id) {
        UUID tenantId = TenantContextHolder.getTenant();
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new OrderServiceException(
                        String.format("No se encontró el cliente con ID '%s'. Verifica que el cliente exista.", id),
                        HttpStatus.NOT_FOUND
                ));

        if (!customer.getTenantId().equals(tenantId)) {
            throw new OrderServiceException(
                    "No tienes permiso para acceder a este cliente.",
                    HttpStatus.FORBIDDEN
            );
        }

        return toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerCreationRequest request) {
        UUID tenantId = TenantContextHolder.getTenant();

        // Geocodificar la dirección
        LatLng coordinates = geocodeAddress(request);

        // Asignar zona si se proporciona
        Zone zone = null;
        if (request.getZoneId() != null && !request.getZoneId().isBlank()) {
            try {
                UUID zoneId = UUID.fromString(request.getZoneId());
                zone = zoneRepository.findByIdAndTenantId(zoneId, tenantId)
                        .orElseThrow(() -> new OrderServiceException(
                                String.format("No se encontró la zona con ID '%s'. Por favor, verifica que la zona exista y esté asignada a tu organización.", request.getZoneId()),
                                HttpStatus.BAD_REQUEST
                        ));
            } catch (IllegalArgumentException ex) {
                throw new OrderServiceException(
                        String.format("El ID de zona '%s' no tiene un formato válido. Debe ser un UUID válido.", request.getZoneId()),
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        Customer customer = Customer.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .latitude(coordinates.getLat())
                .longitude(coordinates.getLon())
                .postalCode(request.getPostalCode())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .doorbell(request.getDoorbell())
                .notes(request.getNotes())
                .type(parseCustomerType(request.getType()))
                .zone(zone)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Customer created with id: {} for tenant: {}", saved.getId(), tenantId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(UUID id, CustomerCreationRequest request) {
        UUID tenantId = TenantContextHolder.getTenant();
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new OrderServiceException(
                        String.format("No se encontró el cliente con ID '%s'. Verifica que el cliente exista.", id),
                        HttpStatus.NOT_FOUND
                ));

        if (!customer.getTenantId().equals(tenantId)) {
            throw new OrderServiceException(
                    "No tienes permiso para actualizar este cliente.",
                    HttpStatus.FORBIDDEN
            );
        }

        // Geocodificar solo si la dirección cambió
        if (!customer.getAddress().equals(request.getAddress()) ||
            !customer.getCity().equals(request.getCity()) ||
            !customer.getState().equals(request.getState()) ||
            !customer.getCountry().equals(request.getCountry()) ||
            !customer.getPostalCode().equals(request.getPostalCode())) {

            LatLng coordinates = geocodeAddress(request);
            customer.setLatitude(coordinates.getLat());
            customer.setLongitude(coordinates.getLon());
        }

        // Actualizar zona si se proporciona
        if (request.getZoneId() != null && !request.getZoneId().isBlank()) {
            try {
                UUID zoneId = UUID.fromString(request.getZoneId());
                Zone zone = zoneRepository.findByIdAndTenantId(zoneId, tenantId)
                        .orElseThrow(() -> new OrderServiceException(
                                String.format("No se encontró la zona con ID '%s'. Por favor, verifica que la zona exista y esté asignada a tu organización.", request.getZoneId()),
                                HttpStatus.BAD_REQUEST
                        ));
                customer.setZone(zone);
            } catch (IllegalArgumentException ex) {
                throw new OrderServiceException(
                        String.format("El ID de zona '%s' no tiene un formato válido. Debe ser un UUID válido.", request.getZoneId()),
                        HttpStatus.BAD_REQUEST
                );
            }
        } else {
            customer.setZone(null); // Remover zona si no se proporciona
        }

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        customer.setPostalCode(request.getPostalCode());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setCountry(request.getCountry());
        customer.setDoorbell(request.getDoorbell());
        customer.setNotes(request.getNotes());
        customer.setType(parseCustomerType(request.getType()));
        customer.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        Customer updated = customerRepository.save(customer);
        log.info("Customer updated with id: {} for tenant: {}", updated.getId(), tenantId);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID id) {
        UUID tenantId = TenantContextHolder.getTenant();
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new OrderServiceException(
                        String.format("No se encontró el cliente con ID '%s'. Verifica que el cliente exista.", id),
                        HttpStatus.NOT_FOUND
                ));

        if (!customer.getTenantId().equals(tenantId)) {
            throw new OrderServiceException(
                    "No tienes permiso para eliminar este cliente.",
                    HttpStatus.FORBIDDEN
            );
        }

        customerRepository.delete(customer);
        log.info("Customer deleted with id: {} for tenant: {}", id, tenantId);
    }

    private LatLng geocodeAddress(CustomerCreationRequest request) {
        try {
            StructuredAddress structuredAddress = new StructuredAddress()
                    .street(request.getAddress())
                    .city(request.getCity())
                    .state(request.getState())
                    .country(request.getCountry())
                    .postalCode(request.getPostalCode());

            LatLng coordinates = geoService.getLatLngStructured(structuredAddress);

            if (coordinates == null) {
                String fullAddress = String.format("%s, %s, %s, %s, %s",
                        request.getAddress(),
                        request.getCity(),
                        request.getState(),
                        request.getCountry(),
                        request.getPostalCode());
                throw new OrderServiceException(
                        String.format("La dirección '%s' no pudo ser validada. Por favor, verifica que todos los datos sean correctos y correspondan a una ubicación real.", fullAddress),
                        HttpStatus.BAD_REQUEST
                );
            }

            return coordinates;
        } catch (OrderServiceException e) {
            // Re-lanzar excepciones de OrderService
            throw e;
        } catch (Exception e) {
            log.error("Error geocoding address: {}", e.getMessage());
            String fullAddress = String.format("%s, %s, %s, %s, %s",
                    request.getAddress(),
                    request.getCity(),
                    request.getState(),
                    request.getCountry(),
                    request.getPostalCode());
            throw new OrderServiceException(
                    String.format("Error al validar la dirección '%s'. %s", fullAddress, e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private Customer.CustomerType parseCustomerType(String type) {
        if (type == null || type.isBlank()) {
            return Customer.CustomerType.REGULAR;
        }
        try {
            return Customer.CustomerType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid customer type '{}', defaulting to REGULAR", type);
            return Customer.CustomerType.REGULAR;
        }
    }

    private CustomerResponse toResponse(Customer customer) {
        CustomerResponse.CustomerResponseBuilder builder = CustomerResponse.builder()
                .id(customer.getId())
                .tenantId(customer.getTenantId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .postalCode(customer.getPostalCode())
                .city(customer.getCity())
                .state(customer.getState())
                .country(customer.getCountry())
                .doorbell(customer.getDoorbell())
                .notes(customer.getNotes())
                .type(customer.getType().name())
                .isActive(customer.getIsActive() != null ? customer.getIsActive() : true)
                .latitude(customer.getLatitude())
                .longitude(customer.getLongitude())
                .createdAt(customer.getCreatedAt());


        // Agregar información de la zona si existe
        if (customer.getZone() != null) {
            builder.zoneId(customer.getZone().getId())
                   .zoneName(customer.getZone().getName());
        }

        return builder.build();
    }
}