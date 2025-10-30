package com.mylogisticcba.core.repository.orders;

import com.mylogisticcba.core.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}

