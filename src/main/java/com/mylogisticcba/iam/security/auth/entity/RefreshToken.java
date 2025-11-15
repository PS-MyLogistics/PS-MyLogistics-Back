package com.mylogisticcba.iam.security.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


@Data
@Entity
@Builder
@AllArgsConstructor

public class RefreshToken {

    @Id
    @Column(nullable = false, unique = true)
    private UUID token;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID userId;


    private Instant expiryDate;

    private boolean revoked;

    private Instant deletedAt;

    public RefreshToken() {

    }

    @PrePersist
    public void prePersist() {
        if (this.token == null) {
            this.token = UUID.randomUUID();
        }
        if (expiryDate == null) {
            expiryDate = Instant.now().plus(7, ChronoUnit.DAYS);
        }
    }

    public Long getMaxAgeInSeconds() {
        if (this.expiryDate == null) {
            return 0L;
        }
        return Duration.between(Instant.now(), expiryDate).getSeconds();}

    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }







}
