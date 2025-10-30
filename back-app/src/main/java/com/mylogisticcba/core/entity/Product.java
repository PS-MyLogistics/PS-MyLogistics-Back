package com.mylogisticcba.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String sku; // Stock Keeping Unit

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /*
    @Column(name = "stock_quantity")
    @Builder.Default
    private Integer stockQuantity = 0;
    */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    private String category;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum ProductStatus {
        ACTIVE,
        INACTIVE,
        OUT_OF_STOCK,
        DISCONTINUED
    }

    // Métodos helper
    /*
    public boolean hasStock(Integer quantity) {
        return this.stockQuantity >= quantity;
    }

    public void decreaseStock(Integer quantity) {
        if (hasStock(quantity)) {
            this.stockQuantity -= quantity;
            if (this.stockQuantity == 0) {
                this.status = ProductStatus.OUT_OF_STOCK;
            }
        } else {
            throw new IllegalStateException("Insufficient stock for product: " + this.name);
        }
    }
    */
    /*
    public void increaseStock(Integer quantity) {
        this.stockQuantity += quantity;
        if (this.stockQuantity > 0 && this.status == ProductStatus.OUT_OF_STOCK) {
            this.status = ProductStatus.ACTIVE;
        }
    }
    */

}