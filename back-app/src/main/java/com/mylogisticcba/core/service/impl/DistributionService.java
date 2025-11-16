package com.mylogisticcba.core.service.impl;

import com.mylogisticcba.core.dto.req.DistributionCreationRequest;
import com.mylogisticcba.core.dto.req.DistributionStatusUpdateRequest;
import com.mylogisticcba.core.dto.response.DistributionResponse;
import com.mylogisticcba.core.entity.Distribution;
import com.mylogisticcba.core.entity.Order;
import com.mylogisticcba.core.entity.Vehicle;
import com.mylogisticcba.core.exceptions.OrderServiceException;
import com.mylogisticcba.core.repository.distribution.DistributionRepository;
import com.mylogisticcba.core.repository.distribution.VehicleRepository;
import com.mylogisticcba.core.repository.orders.OrderRepository;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import com.mylogisticcba.iam.tenant.entity.UserEntity;
import com.mylogisticcba.iam.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mylogisticcba.iam.security.auth.securityCustoms.CustomUserDetails;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DistributionService implements com.mylogisticcba.core.service.DistributionService {

    private final DistributionRepository distributionRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public DistributionService(DistributionRepository distributionRepository,
                               OrderRepository orderRepository,
                               UserRepository userRepository,
                               VehicleRepository vehicleRepository) {
        this.distributionRepository = distributionRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional
    public DistributionResponse createDistribution(DistributionCreationRequest req) {
        if (req == null || req.getOrderIds() == null || req.getOrderIds().isEmpty()) {
            throw new OrderServiceException("orderIds required", HttpStatus.BAD_REQUEST);
        }

        List<Order> orders = orderRepository.findAllById(req.getOrderIds());
        if (orders.size() != req.getOrderIds().size()) {
            // determinar cuales faltan
            List<UUID> foundIds = orders.stream().map(Order::getId).collect(Collectors.toList());
            List<UUID> missing = req.getOrderIds().stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            throw new OrderServiceException("Orders not found: " + missing, HttpStatus.BAD_REQUEST);
        }

        // Determinar tenantId
        UUID tenantId = TenantContextHolder.getTenant();

        // Validar que todas las órdenes pertenezcan al mismo tenant
        boolean allSameTenant = orders.stream().allMatch(o -> tenantId.equals(o.getTenantId()));
        if (!allSameTenant) {
            throw new OrderServiceException("All orders must belong to the same tenant", HttpStatus.BAD_REQUEST);
        }

        // Buscar dealer si se proporcionó
        UserEntity dealer = null;
        if (req.getDealerId() != null) {
            Optional<UserEntity> dealerOpt = userRepository.findByIdAndTenant_Id(req.getDealerId(), tenantId);
            if (dealerOpt.isEmpty()) {
                throw new OrderServiceException("Dealer not found for tenant: " + req.getDealerId(), HttpStatus.BAD_REQUEST);
            }
            dealer = dealerOpt.get();
        }

        // Buscar vehículo si se proporcionó
        Vehicle vehicle = null;
        if (req.getVehicleId() != null) {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(req.getVehicleId());
            if (vehicleOpt.isEmpty()) {
                throw new OrderServiceException("Vehicle not found: " + req.getVehicleId(), HttpStatus.BAD_REQUEST);
            }
            vehicle = vehicleOpt.get();
            if (!tenantId.equals(vehicle.getTenantId())) {
                throw new OrderServiceException("Vehicle does not belong to tenant", HttpStatus.BAD_REQUEST);
            }
        }

        Distribution dist = Distribution.builder()
                .tenantId(tenantId)
                .orders(orders)
                .dealer(dealer)
                .vehicle(vehicle)
                .startProgramDateTime(req.getStartProgramDateTime())
                .endProgramDateTime(req.getEndProgramDateTime())
                .notes(req.getNotes())
                .status(Distribution.DistributionStatus.PLANNED)
                .createdAt(Instant.now())
                .build();

        Distribution saved = distributionRepository.save(dist);

        // Actualizar estado de los pedidos a CONFIRMED
        orders.forEach(order -> {
            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);
        });

        List<UUID> orderIdsAux = new ArrayList<>();
        for (Order o : saved.getOrders()) {
            orderIdsAux.add(o.getId());
        }
        // Construir DTO de respuesta
        DistributionResponse resp = DistributionResponse.builder()
                .id(saved.getId())
                .tenantId(saved.getTenantId())
                .orderIds(orderIdsAux)
                .dealerId(saved.getDealer() == null ? null : saved.getDealer().getId())
                .vehicleId(saved.getVehicle() == null ? null : saved.getVehicle().getId())
                .startProgramDateTime(saved.getStartProgramDateTime())
                .endProgramDateTime(saved.getEndProgramDateTime())
                .createdAt(saved.getCreatedAt())
                .status(saved.getStatus() == null ? null : saved.getStatus().name())
                .notes(saved.getNotes())
                .isOptimized(saved.isOptimized())
                .build();

        return resp;
    }

    @Override
    @Transactional
    public Distribution saveOptimizedSequence(UUID distributionId, List<UUID> optimizedOrderIds) {
        if (distributionId == null) {
            throw new OrderServiceException("distributionId required", HttpStatus.BAD_REQUEST);
        }
        if (optimizedOrderIds == null || optimizedOrderIds.isEmpty()) {
            throw new OrderServiceException("optimizedOrderIds required", HttpStatus.BAD_REQUEST);
        }

        Distribution dist = distributionRepository.findById(distributionId)
                .orElseThrow(() -> new OrderServiceException("Distribution not found: " + distributionId, HttpStatus.NOT_FOUND));

        UUID tenantFromContext = TenantContextHolder.getTenant();
        if (tenantFromContext == null || !tenantFromContext.equals(dist.getTenantId())) {
            throw new OrderServiceException("Invalid tenant context for distribution", HttpStatus.FORBIDDEN);
        }

        // Recuperar órdenes y validar que pertenezcan al tenant
        List<Order> orders = orderRepository.findAllById(optimizedOrderIds);
        if (orders.size() != optimizedOrderIds.size()) {
            List<UUID> found = orders.stream().map(Order::getId).collect(Collectors.toList());
            List<UUID> missing = optimizedOrderIds.stream().filter(id -> !found.contains(id)).collect(Collectors.toList());
            throw new OrderServiceException("Some optimized orders not found: " + missing, HttpStatus.BAD_REQUEST);
        }

        boolean allSameTenant = orders.stream().allMatch(o -> tenantFromContext.equals(o.getTenantId()));
        if (!allSameTenant) {
            throw new OrderServiceException("All optimized orders must belong to tenant: " + tenantFromContext, HttpStatus.BAD_REQUEST);
        }

        // Reordenar las órdenes según optimizedOrderIds y asignar a ordersOptimized
        List<Order> orderedOptimized = new ArrayList<>();
        for (UUID id : optimizedOrderIds) {
            orders.stream().filter(o -> o.getId().equals(id)).findFirst().ifPresent(orderedOptimized::add);
        }

        dist.getOrdersOptimized().clear();
        dist.getOrdersOptimized().addAll(orderedOptimized);

        // Marcar como optimizado y cambiar estado a IN_PROGRESS
        dist.setOptimized(true);
        //dist.setStatus(Distribution.DistributionStatus.IN_PROGRESS);

        return distributionRepository.save(dist);
    }

    @Override
    public Distribution getDistributionById(UUID distributionId) {
        if (distributionId == null) {
            throw new OrderServiceException("distributionId required", HttpStatus.BAD_REQUEST);
        }

        Distribution dist = distributionRepository.findById(distributionId)
                .orElseThrow(() -> new OrderServiceException("Distribution not found: " + distributionId, HttpStatus.NOT_FOUND));

        UUID tenantFromContext = TenantContextHolder.getTenant();
        if (tenantFromContext == null || !tenantFromContext.equals(dist.getTenantId())) {
            throw new OrderServiceException("Invalid tenant context for distribution", HttpStatus.FORBIDDEN);
        }

        return dist;
    }

    @Override
    public List<DistributionResponse> getAll() {

        UUID tenantFromContext = TenantContextHolder.getTenant();
        if (tenantFromContext == null) {
            throw new OrderServiceException("Invalid tenant context for distribution", HttpStatus.FORBIDDEN);
        }

        List<Distribution> dist = distributionRepository.findAllByTenantId(tenantFromContext);
        if (dist == null || dist.isEmpty()) {
            throw new OrderServiceException("No distributions found", HttpStatus.NOT_FOUND);
        }

        // Filtrar por dealer si el usuario autenticado es DEALER
        UUID authenticatedUserId = getAuthenticatedUserId();
        if (authenticatedUserId != null && isUserDealer()) {
            dist = dist.stream()
                    .filter(d -> d.getDealer() != null && d.getDealer().getId().equals(authenticatedUserId))
                    .collect(Collectors.toList());

            if (dist.isEmpty()) {
                throw new OrderServiceException("No distributions found for this dealer", HttpStatus.NOT_FOUND);
            }
        }

        return dist.stream().map(d -> {
            List<UUID> orderIdsAux = d.getOrders() == null
                    ? new ArrayList<>()
                    : d.getOrders().stream().map(Order::getId).collect(Collectors.toList());

            // preparar ordersOptimized y mapa de optimización si corresponde
            List<UUID> ordersOptimizedAux = d.getOrdersOptimized() == null
                    ? new ArrayList<>()
                    : d.getOrdersOptimized().stream().map(Order::getId).collect(Collectors.toList());

            Boolean isOptimized = Boolean.FALSE;
            Map<Integer, UUID> optimizationRoute = null;
            if (d.isOptimized()) {
                isOptimized = Boolean.TRUE;
                optimizationRoute = new LinkedHashMap<>();
                for (int i = 0; i < ordersOptimizedAux.size(); i++) {
                    // usar índice 1-based para la secuencia
                    optimizationRoute.put(i + 1, ordersOptimizedAux.get(i));
                }
            }

            return DistributionResponse.builder()
                    .id(d.getId())
                    .tenantId(d.getTenantId())
                    .orderIds(orderIdsAux)
                    .optimizatedRoute(optimizationRoute)
                    .dealerId(d.getDealer() == null ? null : d.getDealer().getId())
                    .vehicleId(d.getVehicle() == null ? null : d.getVehicle().getId())
                    .startProgramDateTime(d.getStartProgramDateTime())
                    .endProgramDateTime(d.getEndProgramDateTime())
                    .createdAt(d.getCreatedAt())
                    .status(d.getStatus() == null ? null : d.getStatus().name())
                    .notes(d.getNotes())
                    .isOptimized(d.isOptimized())
                    .build();
        }).collect(Collectors.toList());

    }

    @Override
    public DistributionResponse getDistributionByIdDealer(UUID id) {
        if (id == null) {
            throw new OrderServiceException("distributionId required", HttpStatus.BAD_REQUEST);
        }

        Distribution dist = distributionRepository.findById(id)
                .orElseThrow(() -> new OrderServiceException("Distribution not found: " + id, HttpStatus.NOT_FOUND));

        UUID tenantFromContext = TenantContextHolder.getTenant();
        if (tenantFromContext == null || !tenantFromContext.equals(dist.getTenantId())) {
            throw new OrderServiceException("Invalid tenant context for distribution", HttpStatus.FORBIDDEN);
        }

        // Obtener usuario autenticado desde el SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new OrderServiceException("No authenticated user", HttpStatus.FORBIDDEN);
        }

        UUID authenticatedUserId = null;
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            CustomUserDetails cud = (CustomUserDetails) principal;
            if (cud.getUser() != null) {
                authenticatedUserId = cud.getUser().getId();
            }
        }

        if (authenticatedUserId == null) {
            throw new OrderServiceException("Authenticated user id not available", HttpStatus.FORBIDDEN);
        }

        if (dist.getDealer() == null || !authenticatedUserId.equals(dist.getDealer().getId())) {
            throw new OrderServiceException("Distribution does not belong to dealer", HttpStatus.FORBIDDEN);
        }


        // preparar ordersOptimized y mapa de optimización si corresponde
        List<UUID> ordersOptimizedAux = dist.getOrdersOptimized() == null
                ? new ArrayList<>()
                : dist.getOrdersOptimized().stream().map(Order::getId).collect(Collectors.toList());

        Boolean isOptimized = Boolean.FALSE;
        Map<Integer, UUID> optimizationRoute = null;
        if (dist.isOptimized()) {
            isOptimized = Boolean.TRUE;
            optimizationRoute = new LinkedHashMap<>();
            for (int i = 0; i < ordersOptimizedAux.size(); i++) {
                optimizationRoute.put(i + 1, ordersOptimizedAux.get(i));
            }
        }

        DistributionResponse response = DistributionResponse.builder()
                .id(dist.getId())
                .tenantId(dist.getTenantId())
                .orderIds(dist.getOrders().stream().map(o -> o.getId()).collect(Collectors.toList()))
                .optimizatedRoute(optimizationRoute)
                .dealerId(dist.getDealer() == null ? null : dist.getDealer().getId())
                .vehicleId(dist.getVehicle() == null ? null : dist.getVehicle().getId())
                .startProgramDateTime(dist.getStartProgramDateTime())
                .endProgramDateTime(dist.getEndProgramDateTime())
                .createdAt(dist.getCreatedAt())
                .status(dist.getStatus() == null ? null : dist.getStatus().name())
                .notes(dist.getNotes())
                .isOptimized(dist.isOptimized())
                .build();

        return response;

    }

    @Override
    public List<DistributionResponse> getDistributionsForDealer() {
        UUID tenantFromContext = TenantContextHolder.getTenant();
        if (tenantFromContext == null) {
            throw new OrderServiceException("Invalid tenant context for distribution", HttpStatus.FORBIDDEN);
        }

        // Obtener usuario autenticado desde el SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new OrderServiceException("No authenticated user", HttpStatus.FORBIDDEN);
        }

        UUID authenticatedUserId = null;
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            CustomUserDetails cud = (CustomUserDetails) principal;
            if (cud.getUser() != null) {
                authenticatedUserId = cud.getUser().getId();
            }
        }

        if (authenticatedUserId == null) {
            throw new OrderServiceException("Authenticated user id not available", HttpStatus.FORBIDDEN);
        }

        // Make final for lambda usage
        final UUID finalAuthenticatedUserId = authenticatedUserId;

        // Buscar todas las distribuciones del tenant donde el dealer es el usuario autenticado
        List<Distribution> distributions = distributionRepository.findAllByTenantId(tenantFromContext);

        // Filtrar solo las distribuciones asignadas al dealer autenticado
        List<Distribution> dealerDistributions = distributions.stream()
                .filter(d -> d.getDealer() != null && d.getDealer().getId().equals(finalAuthenticatedUserId))
                .collect(Collectors.toList());

        if (dealerDistributions.isEmpty()) {
            throw new OrderServiceException("No distributions found for this dealer", HttpStatus.NOT_FOUND);
        }

        // Convertir a DTO
        return dealerDistributions.stream().map(d -> {
            List<UUID> orderIdsAux = d.getOrders() == null
                    ? new ArrayList<>()
                    : d.getOrders().stream().map(Order::getId).collect(Collectors.toList());

            // preparar ordersOptimized y mapa de optimización si corresponde
            List<UUID> ordersOptimizedAux = d.getOrdersOptimized() == null
                    ? new ArrayList<>()
                    : d.getOrdersOptimized().stream().map(Order::getId).collect(Collectors.toList());

            Boolean isOptimized = Boolean.FALSE;
            Map<Integer, UUID> optimizationRoute = null;
            if (d.isOptimized()) {
                isOptimized = Boolean.TRUE;
                optimizationRoute = new LinkedHashMap<>();
                for (int i = 0; i < ordersOptimizedAux.size(); i++) {
                    optimizationRoute.put(i + 1, ordersOptimizedAux.get(i));
                }
            }

            return DistributionResponse.builder()
                    .id(d.getId())
                    .tenantId(d.getTenantId())
                    .orderIds(orderIdsAux)
                    .optimizatedRoute(optimizationRoute)
                    .dealerId(d.getDealer() == null ? null : d.getDealer().getId())
                    .vehicleId(d.getVehicle() == null ? null : d.getVehicle().getId())
                    .startProgramDateTime(d.getStartProgramDateTime())
                    .endProgramDateTime(d.getEndProgramDateTime())
                    .createdAt(d.getCreatedAt())
                    .status(d.getStatus() == null ? null : d.getStatus().name())
                    .notes(d.getNotes())
                    .isOptimized(d.isOptimized())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DistributionResponse updateDistributionStatus(UUID id, DistributionStatusUpdateRequest request) {
        if (id == null) {
            throw new OrderServiceException("distributionId required", HttpStatus.BAD_REQUEST);
        }

        Distribution dist = distributionRepository.findById(id)
                .orElseThrow(() -> new OrderServiceException("Distribution not found: " + id, HttpStatus.NOT_FOUND));

        UUID tenantFromContext = TenantContextHolder.getTenant();
        if (tenantFromContext == null || !tenantFromContext.equals(dist.getTenantId())) {
            throw new OrderServiceException("Invalid tenant context for distribution", HttpStatus.FORBIDDEN);
        }

        // Actualizar el status si viene en el request
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                Distribution.DistributionStatus newStatus = Distribution.DistributionStatus.valueOf(request.getStatus().toUpperCase());
                dist.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new OrderServiceException(
                        "Invalid status: " + request.getStatus(),
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        // Guardar cambios
        Distribution updated = distributionRepository.save(dist);

        // Construir respuesta
        List<UUID> orderIdsAux = updated.getOrders() == null
                ? new ArrayList<>()
                : updated.getOrders().stream().map(Order::getId).collect(Collectors.toList());

        List<UUID> ordersOptimizedAux = updated.getOrdersOptimized() == null
                ? new ArrayList<>()
                : updated.getOrdersOptimized().stream().map(Order::getId).collect(Collectors.toList());

        Map<Integer, UUID> optimizationRoute = null;
        if (updated.isOptimized()) {
            optimizationRoute = new LinkedHashMap<>();
            for (int i = 0; i < ordersOptimizedAux.size(); i++) {
                optimizationRoute.put(i + 1, ordersOptimizedAux.get(i));
            }
        }

        return DistributionResponse.builder()
                .id(updated.getId())
                .tenantId(updated.getTenantId())
                .orderIds(orderIdsAux)
                .optimizatedRoute(optimizationRoute)
                .dealerId(updated.getDealer() == null ? null : updated.getDealer().getId())
                .vehicleId(updated.getVehicle() == null ? null : updated.getVehicle().getId())
                .startProgramDateTime(updated.getStartProgramDateTime())
                .endProgramDateTime(updated.getEndProgramDateTime())
                .createdAt(updated.getCreatedAt())
                .status(updated.getStatus() == null ? null : updated.getStatus().name())
                .notes(updated.getNotes())
                .isOptimized(updated.isOptimized())
                .build();
    }

    /**
     * Obtiene una distribución por ID con validación de rol.
     * Si el usuario es DEALER, valida que la distribución le pertenezca.
     */
    @Override
    public DistributionResponse getDistributionByIdWithRoleValidation(UUID id) {
        if (id == null) {
            throw new OrderServiceException("distributionId required", HttpStatus.BAD_REQUEST);
        }

        Distribution dist = distributionRepository.findById(id)
                .orElseThrow(() -> new OrderServiceException(
                        String.format("No se encontró la distribución con ID '%s'.", id),
                        HttpStatus.NOT_FOUND
                ));

        UUID tenantFromContext = TenantContextHolder.getTenant();
        if (tenantFromContext == null || !tenantFromContext.equals(dist.getTenantId())) {
            throw new OrderServiceException("Invalid tenant context for distribution", HttpStatus.FORBIDDEN);
        }

        // Si el usuario es DEALER, validar que la distribución le pertenezca
        UUID authenticatedUserId = getAuthenticatedUserId();
        if (authenticatedUserId != null && isUserDealer()) {
            if (dist.getDealer() == null || !authenticatedUserId.equals(dist.getDealer().getId())) {
                throw new OrderServiceException(
                        "No tienes permiso para acceder a esta distribución.",
                        HttpStatus.FORBIDDEN
                );
            }
        }

        // Preparar ordersOptimized y mapa de optimización si corresponde
        List<UUID> ordersOptimizedAux = dist.getOrdersOptimized() == null
                ? new ArrayList<>()
                : dist.getOrdersOptimized().stream().map(Order::getId).collect(Collectors.toList());

        Boolean isOptimized = Boolean.FALSE;
        Map<Integer, UUID> optimizationRoute = null;
        if (dist.isOptimized()) {
            isOptimized = Boolean.TRUE;
            optimizationRoute = new LinkedHashMap<>();
            for (int i = 0; i < ordersOptimizedAux.size(); i++) {
                optimizationRoute.put(i + 1, ordersOptimizedAux.get(i));
            }
        }

        return DistributionResponse.builder()
                .id(dist.getId())
                .tenantId(dist.getTenantId())
                .orderIds(dist.getOrders().stream().map(Order::getId).collect(Collectors.toList()))
                .optimizatedRoute(optimizationRoute)
                .dealerId(dist.getDealer() == null ? null : dist.getDealer().getId())
                .vehicleId(dist.getVehicle() == null ? null : dist.getVehicle().getId())
                .startProgramDateTime(dist.getStartProgramDateTime())
                .endProgramDateTime(dist.getEndProgramDateTime())
                .createdAt(dist.getCreatedAt())
                .status(dist.getStatus() == null ? null : dist.getStatus().name())
                .notes(dist.getNotes())
                .isOptimized(dist.isOptimized())
                .build();
    }

    /**
     * Obtiene el ID del usuario autenticado desde el SecurityContext
     */
    private UUID getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            CustomUserDetails cud = (CustomUserDetails) principal;
            if (cud.getUser() != null) {
                return cud.getUser().getId();
            }
        }
        return null;
    }

    /**
     * Verifica si el usuario autenticado tiene el rol DEALER
     */
    private boolean isUserDealer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_DEALER"));
    }
}
