package com.mylogisticcba.payments.mercadoPago.entity;

import com.mylogisticcba.iam.tenant.entity.UserEntity;
import com.mylogisticcba.payments.mercadoPago.entity.Pago;
import com.mylogisticcba.payments.mercadoPago.model.enums.EstadoFactura;
import com.mylogisticcba.payments.mercadoPago.model.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "facturas")
@Data 
@NoArgsConstructor 
public class Factura implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // --- ID Lógicos (Vínculos a otros microservicios) ---
    // No usamos @ManyToOne aquí porque son servicios separados.
    // Solo guardamos el ID como referencia.
    
    @Column(nullable = false)
    private UUID tenantId; // Vínculo lógico a 'tenant-service'

    private UUID ownerId; // Vínculo lógico a 'iam-service'

    @Column(nullable = false)
    private UUID clienteId; // Vínculo lógico a 'clientes-service'

    // --- Datos propios de la Factura ---

    @Column(nullable = false, unique = true)
    private String numeroFactura; // Ej: "F-001-00001234"

    @CreationTimestamp // Asigna la fecha actual al crear
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaEmision;

    @Column(nullable = false)
    private Double total;

    @Enumerated(EnumType.STRING) // Guarda el nombre del enum (ej: "TARJETA_CREDITO")
    @Column(nullable = false)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoFactura estado;

    private LocalDateTime fechaAcreditacionPago;

    // --- Relación con Pagos ---
    // Una factura puede tener múltiples intentos de pago
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pago> pagos;
}