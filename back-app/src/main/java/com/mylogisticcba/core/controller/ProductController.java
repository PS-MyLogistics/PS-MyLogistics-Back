package com.mylogisticcba.core.controller;

import com.mylogisticcba.core.dto.req.OrderCreationRequest;
import com.mylogisticcba.core.dto.req.ProductCreationRequest;
import com.mylogisticcba.core.dto.response.OrderCreatedResponse;
import com.mylogisticcba.core.dto.response.ProductResponse;
import com.mylogisticcba.core.service.OrderService;
import com.mylogisticcba.core.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/products/")
public class ProductController {

    private final ProductService productService;

    public ProductController( com.mylogisticcba.core.service.impl.ProductService productService) {
        this.productService = productService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @PostMapping("create")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreationRequest request) {
        ProductResponse order = productService.createProduct(request);
        return ResponseEntity.ok(order);
    }

}
