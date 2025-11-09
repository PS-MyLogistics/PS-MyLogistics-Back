package com.mylogisticcba.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private UUID tenantId;
    private UUID customerId;
    private String customerName;
    private String customerAddress;
    private String customerCity;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private String status;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}