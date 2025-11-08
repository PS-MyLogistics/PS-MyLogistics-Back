package com.mylogisticcba.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"orders"})  // ← Excluir lazy
@EqualsAndHashCode(exclude = {"orders"})
public class Customer {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "postal_code")
    private String postalCode;

    private String city;

    private String state;

    private String country;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String doorbell;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CustomerType type = CustomerType.REGULAR;



    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum CustomerType {
        REGULAR,
        VIP,
        WHOLESALE
    }
}