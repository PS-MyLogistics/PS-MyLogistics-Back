package com.mylogisticcba.common.rateLimit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RateLimitAttemptRepository extends JpaRepository<RateLimitAttempt, String> {

    @Modifying
    @Query("DELETE FROM RateLimitAttempt r WHERE r.expiresAt < :now")
    int deleteExpired(@Param("now") Instant now); // Retorna cantidad eliminada

    void deleteByRateLimitKeyAndRateLimitType(String rateLimitKey, String rateLimitType);

    Optional<RateLimitAttempt> findByRateLimitKeyAndExpiresAtAfter(String key, Instant now);
    Optional<RateLimitAttempt> findByRateLimitKeyAndRateLimitTypeAndExpiresAtAfter(String key,String typ, Instant now);
}