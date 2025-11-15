package com.mylogisticcba.iam.security.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordToken {

        @Id
        private UUID id;

        @Column(nullable = false, unique = true)
        private String tokenHash;

        private LocalDateTime expiryDate;

        @Column(nullable = false)
        private UUID userId;
        @Column(nullable = false)
        private UUID tenantId;

        private boolean isRevoked;

        private boolean isUsed;

        private LocalDateTime deletedAt;




    @PrePersist
        public void prePersist() {
            if(this.id == null) {
                this.id = UUID.randomUUID();
            }
            if (this.expiryDate == null) {
                this.expiryDate = LocalDateTime.now().plusDays(1);
            }
        }

    }





