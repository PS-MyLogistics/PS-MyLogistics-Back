package com.mylogisticcba.core.controller;

import com.mylogisticcba.core.dto.req.OrderCreationRequest;
import com.mylogisticcba.core.dto.response.OrderCreatedResponse;
import com.mylogisticcba.core.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders/")
public class OrderController {


    private final OrderService orderService;

    public OrderController( OrderService orderService) {
        this.orderService = orderService;
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @GetMapping("/getAll")
    @PostMapping
    public ResponseEntity<OrderCreatedResponse> createOrder(@Valid @RequestBody OrderCreationRequest request) {
        OrderCreatedResponse order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }
}
