package com.mylogisticcba.core.service;

import com.mylogisticcba.core.dto.req.ProductCreationRequest;
import com.mylogisticcba.core.dto.response.ProductResponse;
import com.mylogisticcba.core.entity.Product;
import com.mylogisticcba.core.repository.orders.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();

    Product saveProduct(Product product);

    ProductResponse createProduct(ProductCreationRequest product);
}

