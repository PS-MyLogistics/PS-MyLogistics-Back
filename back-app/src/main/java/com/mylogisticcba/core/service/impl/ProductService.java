package com.mylogisticcba.core.service.impl;

import com.mylogisticcba.core.dto.req.ProductCreationRequest;
import com.mylogisticcba.core.dto.response.ProductResponse;
import com.mylogisticcba.core.entity.Product;
import com.mylogisticcba.core.repository.orders.ProductRepository;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService implements com.mylogisticcba.core.service.ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Devuelve todos los productos guardados
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Guarda (crea/actualiza) un producto
    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    //mapea dto a entity
    @Transactional
    public ProductResponse createProduct(ProductCreationRequest product) {
        Product newProduct = new Product();
        UUID tenantId = TenantContextHolder.getTenant();
        newProduct.setName(product.getName());
        newProduct.setTenantId(tenantId);
        newProduct.setDescription(product.getDescription());
        newProduct.setSku(product.getSku());
        newProduct.setPrice(BigDecimal.valueOf(product.getPrice()));
        Product saved = saveProduct(newProduct);
        return new ProductResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getPrice()
        );
    }

    public List<ProductResponse> getAllProductsAsDto() {
        UUID tenantId = TenantContextHolder.getTenant();
        return productRepository.findAll().stream()
                .filter(product -> product.getTenantId().equals(tenantId))
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice()
                ))
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(UUID id) {
        UUID tenantId = TenantContextHolder.getTenant();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        if (!product.getTenantId().equals(tenantId)) {
            throw new EntityNotFoundException("Product not found in this tenant");
        }

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice()
        );
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductCreationRequest request) {
        UUID tenantId = TenantContextHolder.getTenant();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        if (!product.getTenantId().equals(tenantId)) {
            throw new EntityNotFoundException("Product not found in this tenant");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setPrice(BigDecimal.valueOf(request.getPrice()));

        Product updated = productRepository.save(product);
        return new ProductResponse(
                updated.getId(),
                updated.getName(),
                updated.getDescription(),
                updated.getPrice()
        );
    }

    @Transactional
    public void deleteProduct(UUID id) {
        UUID tenantId = TenantContextHolder.getTenant();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        if (!product.getTenantId().equals(tenantId)) {
            throw new EntityNotFoundException("Product not found in this tenant");
        }

        productRepository.delete(product);
    }

}
