package com.mylogisticcba.core.repository.orders;

import com.mylogisticcba.core.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /**
     * Encuentra clientes por lista de IDs y tenant ID
     */
    @Query("SELECT c FROM Customer c WHERE c.id IN :customerIds AND c.tenantId = :tenantId")
    List<Customer> findByIdInAndTenantId(@Param("customerIds") List<UUID> customerIds,
                                         @Param("tenantId") UUID tenantId);

    /**
     * Encuentra todos los clientes de un tenant
     */
    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId")
    List<Customer> findByTenantId(@Param("tenantId") UUID tenantId);
}

