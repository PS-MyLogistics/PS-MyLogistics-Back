package com.mylogisticcba.iam.tenant.entity;

import com.mylogisticcba.iam.tenant.enums.Role;
import com.mylogisticcba.iam.tenant.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username", "tenant_id"})
)
public class UserEntity {
    @Id
    UUID id;

    @Column(nullable = false)
    String username;

    @Column(nullable = false)
    String email;

    @Column(nullable = false)
    String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    UserStatus status;

    String  telephone;

    String  address;

    String  city;

    String  stateOrProvince;

    boolean owner;

    UUID globalTokenVersion;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private List<Role> roles = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private com.mylogisticcba.core.entity.Vehicle vehicle;


    @PrePersist
    public void prePersist() {
        this.id = UUID.randomUUID(); // Genera un UUID automaticamente antes de persistir
    }


}

