package com.mylogisticcba.core.service;

import com.mylogisticcba.core.dto.req.ProductCreationRequest;
import com.mylogisticcba.core.dto.response.ProductResponse;
import com.mylogisticcba.core.entity.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<Product> getAllProducts();

    Product saveProduct(Product product);

    ProductResponse createProduct(ProductCreationRequest product);

    List<ProductResponse> getAllProductsAsDto();

    ProductResponse getProductById(UUID id);

    ProductResponse updateProduct(UUID id, ProductCreationRequest request);

    void deleteProduct(UUID id);
}

