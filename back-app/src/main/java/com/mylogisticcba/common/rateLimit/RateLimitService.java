package com.mylogisticcba.common.rateLimit;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
@Service
public class RateLimitService {
    private final RateLimitAttemptRepository repository;

    /**
     * Verifica si se superó el límite
     */
    public boolean isLimitExceeded(RateLimitType type, String key, int maxAttempts) {
        Instant now = Instant.now();

        Optional<RateLimitAttempt> attempt = repository
                .findByRateLimitKeyAndRateLimitTypeAndExpiresAtAfter(key,type.getPrefix() ,now);

        if (attempt.isPresent()) {
            boolean exceeded = attempt.get().getAttemptCount() >= maxAttempts;
            if (exceeded) {
                log.warn("{} limit exceeded for key: {} with {} attempts",
                        type.getDescription(), key, attempt.get().getAttemptCount());
            }
            return exceeded;
        }

        return false;
    }

    /**
     * Registra un intento fallido
     */
    public void recordFailedAttempt(RateLimitType type, String key, Duration lockoutDuration) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(lockoutDuration);

        Optional<RateLimitAttempt> existing = repository
                .findByRateLimitKeyAndRateLimitTypeAndExpiresAtAfter(key,type.getPrefix(),now);

        if (existing.isPresent()) {
            // Incrementar contador existente
            RateLimitAttempt attempt = existing.get();
            attempt.setAttemptCount(attempt.getAttemptCount() + 1);
            attempt.setExpiresAt(expiresAt);
            attempt.setLastAttempt(now);
            repository.save(attempt);

            log.debug("Incremented {} attempts for key: {} to {}",
                    type.getDescription(), key, attempt.getAttemptCount());
        } else {
            // Crear nuevo registro
            RateLimitAttempt newAttempt = RateLimitAttempt.builder()
                    .rateLimitKey(key)
                    .attemptCount(1)
                    .lastAttempt(now)
                    .expiresAt(expiresAt)
                    .rateLimitType(type.getPrefix())
                    .build();

            repository.save(newAttempt);
            log.debug("Created new {} attempt record for key: {}", type.getDescription(), key);
        }
    }

    /**
     * Limpia intentos (login exitoso)
     */
    public void clearAttempts(String key,RateLimitType rateLimitType) {
        repository.deleteByRateLimitKeyAndRateLimitType(key,rateLimitType.getPrefix());
        log.debug("Cleared attempts for key: {}", key);
    }

    /**
     * Obtiene información del rate limit
     */
    public RateLimitInfo getRateLimitInfo(String key,RateLimitType type) {
        Instant now = Instant.now();

        Optional<RateLimitAttempt> attempt = repository
                .findByRateLimitKeyAndRateLimitTypeAndExpiresAtAfter(key,type.getPrefix(), now);

        if (attempt.isPresent()) {
            RateLimitAttempt rateLimitAttempt = attempt.get();
            long secondsRemaining = Duration.between(now, rateLimitAttempt.getExpiresAt()).getSeconds();

            return RateLimitInfo.builder()
                    .key(key)
                    .currentAttempts(rateLimitAttempt.getAttemptCount())
                    .timeRemainingSeconds(Math.max(0, secondsRemaining))
                    .lastAttempt(rateLimitAttempt.getLastAttempt())
                    .build();
        }

        return RateLimitInfo.builder()
                .key(key)
                .currentAttempts(0)
                .timeRemainingSeconds(0)
                .build();
    }


    public boolean isCurrentlyLocked(String key, RateLimitType type) {
        Instant now = Instant.now();
        return repository.findByRateLimitKeyAndRateLimitTypeAndExpiresAtAfter(key, type.getPrefix(), now).isPresent();
    }

}


