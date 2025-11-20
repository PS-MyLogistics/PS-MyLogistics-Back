package com.mylogisticcba.iam.tenant.entity;

import com.mylogisticcba.iam.tenant.enums.PlanType;
import com.mylogisticcba.iam.tenant.enums.TenantStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tenants")
public class TenantEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String contactEmail;

    private String contactPhone;


    @Column(name = "owner_id")
    private UUID ownerId;


    private String address;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    private PlanType planType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TenantStatus status;
    private int maxUsers;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Fecha en la que finaliza el periodo Premium del tenant (nullable)
    private LocalDateTime endDatePremium;

    @PrePersist
    public void prePersist() {
        this.id = UUID.randomUUID();
    }


}
