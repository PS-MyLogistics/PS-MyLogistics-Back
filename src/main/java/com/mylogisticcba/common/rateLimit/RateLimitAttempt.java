package com.mylogisticcba.common.rateLimit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "rate_limit_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateLimitAttempt {

    @Id
    private String rateLimitKey; // tenant:username

    @Column(nullable = false)
    private Integer attemptCount;

    @Column(nullable = false)
    private Instant lastAttempt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(length = 50)
    private String rateLimitType; // "LOGIN", "PASSWORD_RESET", etc.
}