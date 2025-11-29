package com.mylogisticcba.common.rateLimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitCleanUpJob {
    private final RateLimitAttemptRepository repository;
    /**
     * Limpia registros expirados
     */
    @Scheduled(fixedDelay = 300000) // cada 5 minutos
    @Transactional
    public void cleanupExpiredAttempts() {
        Instant now = Instant.now();
        repository.deleteExpired(now);
        log.debug("Cleaned up expired rate limit attempts");
    }
}
