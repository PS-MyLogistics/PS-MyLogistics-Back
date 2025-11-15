package com.mylogisticcba.core.repository.orders;

import com.mylogisticcba.core.entity.ContactCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface ContactCustomerRepository extends JpaRepository<ContactCustomer, UUID> {
}

