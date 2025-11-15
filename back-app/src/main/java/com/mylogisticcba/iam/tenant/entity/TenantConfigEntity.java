package com.mylogisticcba.iam.tenant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@Entity
@Table(name = "tenant_config")
@AllArgsConstructor
@NoArgsConstructor
public class TenantConfigEntity {


    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;


    @Column(name = "config_key",nullable = false)
    private String key;

    @Column(name = "config_value",nullable = false)
    private String value;

    // getters & setters
    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}


