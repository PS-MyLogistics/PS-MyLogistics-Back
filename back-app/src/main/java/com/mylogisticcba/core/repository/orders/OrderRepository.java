package com.mylogisticcba.core.repository.orders;

import com.mylogisticcba.core.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
}

