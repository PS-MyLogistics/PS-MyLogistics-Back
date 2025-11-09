package com.mylogisticcba.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String postalCode;
    private String city;
    private String state;
    private String country;
    private String doorbell;
    private String notes;
    private String type;
    private Boolean isActive;
    private Instant createdAt;
}