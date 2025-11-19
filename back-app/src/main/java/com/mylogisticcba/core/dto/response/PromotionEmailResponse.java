package com.mylogisticcba.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionEmailResponse {
    private boolean success;
    private int sentCount;
    private int failedCount;
    private String message;
}
