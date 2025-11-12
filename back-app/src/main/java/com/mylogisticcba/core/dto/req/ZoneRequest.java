package com.mylogisticcba.core.dto.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZoneRequest {

    @NotEmpty(message = "El nombre de la zona no puede estar vacío")
    private String name;

    private String description;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe estar en formato hexadecimal #RRGGBB")
    private String color;

    private Boolean isActive;
}