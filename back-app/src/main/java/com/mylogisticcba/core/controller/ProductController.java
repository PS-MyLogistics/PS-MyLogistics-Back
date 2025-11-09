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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/products/")
public class ProductController {

    private final ProductService productService;

    public ProductController( com.mylogisticcba.core.service.impl.ProductService productService) {
        this.productService = productService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @GetMapping("/getAll")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProductsAsDto();
        return ResponseEntity.ok(products);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        ProductResponse product = productService.getProductById(UUID.fromString(id));
        return ResponseEntity.ok(product);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @PostMapping("create")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreationRequest request) {
        ProductResponse order = productService.createProduct(request);
        return ResponseEntity.ok(order);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String id, @Valid @RequestBody ProductCreationRequest request) {
        ProductResponse updated = productService.updateProduct(UUID.fromString(id), request);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(UUID.fromString(id));
        return ResponseEntity.noContent().build();
    }

}
