package com.mylogisticcba.core.repository.orders;

import com.mylogisticcba.core.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}

