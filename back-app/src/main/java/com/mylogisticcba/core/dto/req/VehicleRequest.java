package com.mylogisticcba.core.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleRequest {

    @NotBlank(message = "Plate is required")
    private String plate;

    private String model;

    @NotNull(message = "Capacity is required")
    private Integer capacity;
}