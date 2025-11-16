package com.mylogisticcba.common.rateLimit;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RateLimitType {
    private String prefix;
    private String description;

    public static RateLimitType of(String prefix) {
        return new RateLimitType(prefix + ":", prefix);
    }

    public static RateLimitType of(String prefix, String description) {
        return new RateLimitType(prefix + ":", description);
    }
}
