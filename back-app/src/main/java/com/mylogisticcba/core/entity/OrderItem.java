package com.mylogisticcba.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"order", "product"})  // ← Excluir lazy
@EqualsAndHashCode(exclude = {"order", "product"})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Calcular subtotal automáticamente
    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
