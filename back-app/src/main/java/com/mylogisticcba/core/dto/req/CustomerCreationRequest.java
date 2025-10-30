package com.mylogisticcba.core.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerCreationRequest {

    private UUID tenantId; // opcional, pero necesario si la entidad lo requiere

    @NotEmpty(message = "El nombre del cliente no puede estar vacío")
    private String name;

    @Email(message = "Email inválido")
    @NotEmpty(message = "El email no puede estar vacío")
    private String email;

    @NotEmpty(message = "El telefono no puede estar vacío")
    private String phoneNumber;

    @NotEmpty(message = "La dirección no puede estar vacía")
    private String address;


    @NotEmpty(message = "El codigo postal no puede estar vacío")
    private String postalCode;

    @NotEmpty(message = "La ciudad no puede estar vacío")
    private String city;
    @NotEmpty(message = "La provincia no puede estar vacía")
    private String state;

    @NotEmpty(message = "El pais no puede estar vacío")
    private String country;

    private String notes;
    private String type; // REGULAR, VIP, WHOLESALE
    private Boolean isActive;
}

