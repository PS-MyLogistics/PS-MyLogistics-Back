package com.mylogisticcba.core.entity;

import com.mylogisticcba.iam.tenant.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"orders","ordersOptimized"})
@EqualsAndHashCode(exclude = {"orders","ordersOptimized"})
@Table(name = "distributions")
public class Distribution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    // Lista de órdenes pertenecientes a esta distribución
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "distribution_orders",
            joinColumns = @JoinColumn(name = "distribution_id"),
            inverseJoinColumns = @JoinColumn(name = "order_id")
    )
    private List<Order> orders = new ArrayList<>();


    // Lista de órdenes optimizadas (secuencia resultado de optimización)
    // Usar tabla de join separada para evitar colisiones con 'orders'
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "distribution_orders_optimized",
            joinColumns = @JoinColumn(name = "distribution_id"),
            inverseJoinColumns = @JoinColumn(name = "order_id")
    )
    private List<Order> ordersOptimized = new ArrayList<>();

    @Column(name = "is_optimized")
    private boolean isOptimized;

    // Dealer (usuario responsable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    private UserEntity dealer;

    // Fecha y hora programada de inicio
    @Column(name = "start_program_date_time")
    private LocalDateTime startProgramDateTime;

    // Fecha y hora programada de cierre
    @Column(name = "end_program_date_time")
    private LocalDateTime endProgramDateTime;

    // Fecha de creación
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Vehículo asignado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DistributionStatus status = DistributionStatus.PLANNED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum DistributionStatus {
        PLANNED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    // Helpers
    public void addOrder(Order order) {
        this.orders.add(order);
    }

    public void removeOrder(Order order) {
        this.orders.remove(order);
    }

    public void addOptimizedOrder(Order order) {
        this.ordersOptimized.add(order);
    }

    public void removeOptimizedOrder(Order order) {
        this.ordersOptimized.remove(order);
    }

}
