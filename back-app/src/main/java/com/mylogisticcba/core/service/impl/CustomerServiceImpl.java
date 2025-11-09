package com.mylogisticcba.core.service.impl;

import com.mylogisticcba.core.dto.req.CustomerCreationRequest;
import com.mylogisticcba.core.dto.response.CustomerResponse;
import com.mylogisticcba.core.entity.Customer;
import com.mylogisticcba.core.repository.orders.CustomerRepository;
import com.mylogisticcba.core.service.CustomerService;
import com.mylogisticcba.core.service.rest.LatLng;
import com.mylogisticcba.core.service.rest.NominatimGeoService;
import com.mylogisticcba.core.service.rest.StructuredAddress;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final NominatimGeoService geoService;

    public CustomerServiceImpl(CustomerRepository customerRepository, NominatimGeoService geoService) {
        this.customerRepository = customerRepository;
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
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        if (!customer.getTenantId().equals(tenantId)) {
            throw new EntityNotFoundException("Customer not found in this tenant");
        }

        return toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerCreationRequest request) {
        UUID tenantId = TenantContextHolder.getTenant();

        // Geocodificar la dirección
        LatLng coordinates = geocodeAddress(request);

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
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        if (!customer.getTenantId().equals(tenantId)) {
            throw new EntityNotFoundException("Customer not found in this tenant");
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

        Customer updated = customerRepository.save(customer);
        log.info("Customer updated with id: {} for tenant: {}", updated.getId(), tenantId);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID id) {
        UUID tenantId = TenantContextHolder.getTenant();
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        if (!customer.getTenantId().equals(tenantId)) {
            throw new EntityNotFoundException("Customer not found in this tenant");
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

            return geoService.getLatLngStructured(structuredAddress);
        } catch (Exception e) {
            log.error("Error geocoding address: {}", e.getMessage());
            throw new RuntimeException("Error geocoding address: " + e.getMessage(), e);
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
        return CustomerResponse.builder()
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
                .isActive(true) // El modelo actual no tiene isActive, siempre true
                .createdAt(customer.getCreatedAt())
                .build();
    }
}