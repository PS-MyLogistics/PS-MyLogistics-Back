package com.mylogisticcba.core.mercadopago.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {

    @Id
    private String id;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Double transactionAmount;

    @Column(nullable = false)
    private String currency;

    private String externalReference;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
