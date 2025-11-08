package com.mylogisticcba.core.service.impl;

import com.mylogisticcba.core.dto.req.ProductCreationRequest;
import com.mylogisticcba.core.dto.response.ProductResponse;
import com.mylogisticcba.core.entity.Product;
import com.mylogisticcba.core.repository.orders.ProductRepository;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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
    public ProductResponse createProduct(ProductCreationRequest product) {
        Product newProduct = new Product();
        UUID tenantId = TenantContextHolder.getTenant();
        newProduct.setName(product.getName());
        newProduct.setTenantId(tenantId);
        newProduct.setDescription(product.getDescription());
        newProduct.setPrice(BigDecimal.valueOf( product.getPrice()));
        Product saved= saveProduct(newProduct);
        return new ProductResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getPrice()
        );
    }

}
