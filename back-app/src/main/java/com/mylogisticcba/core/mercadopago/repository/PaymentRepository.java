package com.mylogisticcba.core.mercadopago.repository;

import com.mylogisticcba.core.mercadopago.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
}
