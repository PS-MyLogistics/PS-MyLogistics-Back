package com.mylogisticcba.core.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreationRequest {

    // customerId ahora es opcional: si es null se usará customerCreationRequest para crear el cliente
    private UUID customerId;

    @NotEmpty(message = "La lista de ítems no puede estar vacía")
    @Valid
    private List<OrderItemRequest> items;

    private String notes;

    @Valid
    private CustomerCreationRequest customerCreationRequest; // puede ser null

}