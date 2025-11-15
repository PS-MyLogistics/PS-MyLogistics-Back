package com.mylogisticcba.core.controller;

import com.mylogisticcba.core.dto.req.OrderCreationRequest;
import com.mylogisticcba.core.dto.response.OrderCreatedResponse;
import com.mylogisticcba.core.dto.response.OrderResponse;
import com.mylogisticcba.core.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders/")
public class OrderController {


    private final OrderService orderService;

    public OrderController( OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DEALER')")
    @GetMapping("/getAll")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DEALER')")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String id) {
        OrderResponse order = orderService.getOrderById(UUID.fromString(id));
        return ResponseEntity.ok(order);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @PostMapping("create")
    public ResponseEntity<OrderCreatedResponse> createOrder(@Valid @RequestBody OrderCreationRequest request) {
        OrderCreatedResponse order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }

}
