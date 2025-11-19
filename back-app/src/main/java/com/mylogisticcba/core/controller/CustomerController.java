package com.mylogisticcba.core.controller;

import com.mylogisticcba.core.dto.req.CustomerCreationRequest;
import com.mylogisticcba.core.dto.response.CustomerResponse;
import com.mylogisticcba.core.dto.response.CustomerWithLastOrderResponse;
import com.mylogisticcba.core.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DEALER')")
    @GetMapping("/getAll")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DEALER')")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable String id) {
        CustomerResponse customer = customerService.getCustomerById(UUID.fromString(id));
        return ResponseEntity.ok(customer);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DEALER')")
    @PostMapping("/create")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreationRequest request) {
        CustomerResponse customer = customerService.createCustomer(request);
        return ResponseEntity.ok(customer);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DEALER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody CustomerCreationRequest request) {
        CustomerResponse updated = customerService.updateCustomer(UUID.fromString(id), request);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
        customerService.deleteCustomer(UUID.fromString(id));
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene la lista de clientes con información de su último pedido
     * @param daysSinceLastOrder Número de días desde el último pedido (opcional, 0 = todos)
     * @return Lista de clientes con información del último pedido
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DEALER')")
    @GetMapping("/with-last-order")
    public ResponseEntity<List<CustomerWithLastOrderResponse>> getCustomersWithLastOrder(
            @RequestParam(required = false, defaultValue = "0") Integer daysSinceLastOrder) {
        List<CustomerWithLastOrderResponse> customers = customerService.getCustomersWithLastOrder(daysSinceLastOrder);
        return ResponseEntity.ok(customers);
    }
}