package com.mylogisticcba.common.rateLimit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
@Builder
@Data
@AllArgsConstructor
public class RateLimitInfo {
    private String key;
    private int currentAttempts;
    private long timeRemainingSeconds;
    private Instant lastAttempt;

    public boolean isBlocked(int maxAttempts) {
        return currentAttempts >= maxAttempts;
    }
}
