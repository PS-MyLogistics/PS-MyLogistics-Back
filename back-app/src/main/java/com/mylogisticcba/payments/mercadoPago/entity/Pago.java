package com.mylogisticcba.payments.mercadoPago.entity;

import com.mylogisticcba.payments.mercadoPago.model.enums.EstadoPago;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
public class Pago implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // --- Relación Fuerte (FK) ---
    // Esta SÍ es una relación fuerte (Foreign Key) porque
    // Pago y Factura viven en la misma BBDD.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    private String idPreference; // Para guardar el idPreference de MercadoPago

    // --- Datos propios del Pago ---

    @Column(nullable = false)
    private Double monto;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estadoPago;

    /**
     * ID de la transacción devuelto por el gateway de pagos externo
     * (Ej: MercadoPago). Es crucial para conciliaciones.
     */
    @Column(unique = true)
    private String transaccionIdExterno;
}