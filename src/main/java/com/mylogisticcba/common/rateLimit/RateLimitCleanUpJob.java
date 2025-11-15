package com.mylogisticcba.common.rateLimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
@Slf4j
@Component
public class RateLimitCleanUpJob {
    RateLimitAttemptRepository repository;
    /**
     * Limpia registros expirados
     */
    @Scheduled(fixedDelay = 300000) // cada 5 minutos
    public void cleanupExpiredAttempts() {
        Instant now = Instant.now();
        repository.deleteExpired(now);
        log.debug("Cleaned up expired rate limit attempts");
    }
}
