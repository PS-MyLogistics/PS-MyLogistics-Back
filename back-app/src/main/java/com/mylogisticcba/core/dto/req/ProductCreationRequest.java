package com.mylogisticcba.core.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//dto para la creacion de productos
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreationRequest {
    private String name;
    private String description;
    private String sku; // Opcional - se genera automáticamente si está vacío
    private Double price;
}
