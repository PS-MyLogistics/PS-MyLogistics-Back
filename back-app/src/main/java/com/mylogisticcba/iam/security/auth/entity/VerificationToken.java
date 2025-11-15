package com.mylogisticcba.iam.security.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class VerificationToken {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID token;

    private Instant expiryDate;

    @Column(nullable = false)
    private UUID userId;
    @Column(nullable = false)
    private UUID tenantId;

    private boolean isDeleted;

    private Instant deletedAt;


    @PrePersist
    public void prePersist() {
        if(this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.expiryDate == null) {
            this.expiryDate = Instant.now().plus(1, ChronoUnit.DAYS);
        }
    }

}



