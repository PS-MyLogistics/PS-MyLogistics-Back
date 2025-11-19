package com.mylogisticcba.core.repository.orders;

import com.mylogisticcba.core.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Encuentra el último pedido de un cliente específico
     */
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.tenantId = :tenantId ORDER BY o.createdAt DESC")
    List<Order> findLatestByCustomerIdAndTenantId(@Param("customerId") UUID customerId,
                                                   @Param("tenantId") UUID tenantId,
                                                   Pageable pageable);

    /**
     * Cuenta el total de pedidos de un cliente específico
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.id = :customerId AND o.tenantId = :tenantId")
    Long countByCustomerIdAndTenantId(@Param("customerId") UUID customerId,
                                      @Param("tenantId") UUID tenantId);
}

