package com.mylogisticcba.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerWithLastOrderResponse {
    private UUID customerId;
    private String customerName;
    private String email;
    private String phoneNumber;
    private String address;
    private String type;

    // Información del último pedido
    private UUID lastOrderId;
    private String lastOrderNumber;
    private Instant lastOrderDate;
    private BigDecimal lastOrderAmount;
    private String lastOrderStatus;
    private Long daysSinceLastOrder;
    private Long totalOrders;
}
