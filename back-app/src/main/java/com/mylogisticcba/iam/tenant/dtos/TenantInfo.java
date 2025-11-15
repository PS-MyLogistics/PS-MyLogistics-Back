package com.mylogisticcba.iam.tenant.dtos;

import com.mylogisticcba.iam.tenant.enums.PlanType;
import com.mylogisticcba.iam.tenant.enums.TenantStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@AllArgsConstructor
@Data
@NoArgsConstructor
public class TenantInfo {



        private String name;

        private String contactEmail;

        private String contactPhone;


        private String ownerName;


        private String address;

        private boolean active = true;

        private PlanType planType;


        private TenantStatus status;
        private int maxUsers;

        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();






}
