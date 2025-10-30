package com.mylogisticcba.core.repository.orders;

import com.mylogisticcba.core.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
}

