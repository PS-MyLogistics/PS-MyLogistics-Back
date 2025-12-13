package com.mylogisticcba.core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylogisticcba.core.dto.req.OptimizationRequest;
import com.mylogisticcba.core.entity.Distribution;
import com.mylogisticcba.core.entity.Order;
import com.mylogisticcba.core.repository.distribution.DistributionRepository;
import com.mylogisticcba.core.service.rest.RestClientService;
import com.mylogisticcba.core.repository.orders.OrderRepository;
import com.mylogisticcba.core.service.DistributionService;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OptimizationService {

    private static final Logger log = LoggerFactory.getLogger(OptimizationService.class);

    private final RestClientService restClientService;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final DistributionRepository distributionRepository;
    private final DistributionService distributionService;

    // API key (mismo valor usado previamente en el controller)
    private static final String ORS_API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImU4OTAzYTNhNDk5ZjQyYzBiNGRlMTA3Nzg5NGYxNTEzIiwiaCI6Im11cm11cjY0In0=";
    private static final String ORS_OPTIMIZATION_URL = "https://api.openrouteservice.org/optimization";

    public OptimizationService(RestClientService restClientService,
                               ObjectMapper objectMapper,
                               OrderRepository orderRepository,
                               DistributionRepository distributionRepository,
                               DistributionService distributionService) {
        this.restClientService = restClientService;
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
        this.distributionRepository = distributionRepository;
        this.distributionService = distributionService;
    }

    @Transactional
    public JsonNode optimizeRoutes(UUID distributionId) throws Exception {
        if (distributionId == null) {
            throw new IllegalArgumentException("distributionId required");
        }

        Distribution dist = distributionRepository.findById(distributionId)
                .orElseThrow(() -> new IllegalArgumentException("Distribution not found: " + distributionId));

        UUID tenantFromContext = TenantContextHolder.getTenant();
        if (tenantFromContext == null || !tenantFromContext.equals(dist.getTenantId())) {
            throw new IllegalArgumentException("Invalid tenant context for distribution");
        }

        List<Order> orders = dist.getOrders();
        if (orders == null || orders.isEmpty()) {
            throw new IllegalArgumentException("Distribution has no orders");
        }

        // Ordenar las órdenes por ID para garantizar consistencia
        orders = orders.stream()
                .sorted((o1, o2) -> o1.getId().compareTo(o2.getId()))
                .collect(Collectors.toList());

        log.info("Starting route optimization for distribution {} with {} orders", distributionId, orders.size());

        // Construir jobs a partir de orders -> customer lat/lon
        List<Map<String, Object>> jobs = new ArrayList<>();
        Map<Integer, UUID> jobIdToOrderId = new LinkedHashMap<>();
        int jobId = 1;
        for (Order o : orders) {
            if (o.getCustomer() == null) {
                throw new IllegalArgumentException("Order has no customer: " + o.getId());
            }
            Double lat = o.getCustomer().getLatitude();
            Double lon = o.getCustomer().getLongitude();
            if (lat == null || lon == null) {
                throw new IllegalArgumentException("Customer coordinates missing for order: " + o.getId());
            }

            Map<String, Object> job = new LinkedHashMap<>();
            job.put("id", jobId);
            job.put("location", Arrays.asList(lon, lat)); // ORS espera [lon, lat]
            job.put("service", 300); // 5 minutos de servicio por defecto
            job.put("delivery", Arrays.asList(1)); // Cantidad a entregar
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("orderId", o.getId().toString());
            props.put("customerName", o.getCustomer().getName());
            job.put("properties", props);

            jobs.add(job);
            jobIdToOrderId.put(jobId, o.getId());
            log.info("Job {}: Order {} - Customer '{}' at [{}, {}]",
                jobId, o.getId(), o.getCustomer().getName(), lon, lat);
            jobId++;
        }

        // Calcular centroide de todas las ubicaciones para el punto de inicio del vehículo
        double avgLon = 0.0;
        double avgLat = 0.0;
        for (Map<String, Object> job : jobs) {
            List<Double> loc = (List<Double>) job.get("location");
            avgLon += loc.get(0);
            avgLat += loc.get(1);
        }
        avgLon /= jobs.size();
        avgLat /= jobs.size();
        List<Double> centroidCoord = Arrays.asList(avgLon, avgLat);

        // Crear vehículo con centroide como punto de inicio para ruta abierta (sin retorno al inicio)
        List<Map<String, Object>> vehicles = new ArrayList<>();
        Map<String, Object> vehicle = new LinkedHashMap<>();
        vehicle.put("id", 1);
        vehicle.put("profile", "driving-car");
        vehicle.put("start", centroidCoord);  // Punto de inicio en el centroide
        // NO especificamos "end" para permitir ruta abierta (no vuelve al inicio)
        vehicle.put("capacity", Arrays.asList(100));
        vehicles.add(vehicle);

        log.info("Vehicle starts at centroid: {}, open route (no return to start)", centroidCoord);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("jobs", jobs);
        body.put("vehicles", vehicles);

        log.info("Sending optimization request with {} jobs and {} vehicles", jobs.size(), vehicles.size());
        log.debug("Request body: {}", objectMapper.writeValueAsString(body));

        Map<String, String> headers = Map.of("Authorization", ORS_API_KEY, "Content-Type", "application/json");

        String resp = restClientService.postWithHeaders(ORS_OPTIMIZATION_URL, body, headers, String.class);
        JsonNode root = objectMapper.readTree(resp);

        log.info("Received response from ORS API");
        log.debug("Response body: {}", resp);

        // Extraer orden optimizada: buscar routes -> steps -> tipo job
        List<Integer> optimizedJobOrder = new ArrayList<>();
        if (root.has("routes")) {
            log.debug("Extracting optimized order from 'routes' field");
            for (JsonNode route : root.get("routes")) {
                if (route.has("steps")) {
                    for (JsonNode step : route.get("steps")) {
                        String type = step.path("type").asText("");
                        if ("job".equals(type)) {
                            if (step.has("id")) {
                                optimizedJobOrder.add(step.get("id").asInt());
                            } else if (step.has("job")) {
                                optimizedJobOrder.add(step.get("job").asInt());
                            }
                        }
                    }
                }
            }
        }

        // Si no encontramos por 'routes', intentar 'solutions' -> routes
        if (optimizedJobOrder.isEmpty() && root.has("solutions")) {
            log.debug("'routes' field empty, trying 'solutions' field");
            for (JsonNode sol : root.get("solutions")) {
                if (sol.has("routes")) {
                    for (JsonNode route : sol.get("routes")) {
                        if (route.has("steps")) {
                            for (JsonNode step : route.get("steps")) {
                                String type = step.path("type").asText("");
                                if ("job".equals(type)) {
                                    if (step.has("id")) {
                                        optimizedJobOrder.add(step.get("id").asInt());
                                    } else if (step.has("job")) {
                                        optimizedJobOrder.add(step.get("job").asInt());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (optimizedJobOrder.isEmpty()) {
            log.warn("No optimized job order found in API response for distribution {}", distributionId);
            throw new IllegalStateException("ORS API did not return an optimized route");
        }

        log.info("Optimized job order extracted: {}", optimizedJobOrder);

        // Mapear job ids a order UUIDs
        List<UUID> optimizedOrderIds = new ArrayList<>();
        for (Integer jid : optimizedJobOrder) {
            UUID oid = jobIdToOrderId.get(jid);
            if (oid != null) {
                optimizedOrderIds.add(oid);
            } else {
                log.warn("Job ID {} not found in jobIdToOrderId mapping", jid);
            }
        }

        log.info("Saving optimized sequence with {} orders for distribution {}", optimizedOrderIds.size(), distributionId);

        // Guardar secuencia optimizada en la distribution
        distributionService.saveOptimizedSequence(distributionId, optimizedOrderIds);

        log.info("Route optimization completed successfully for distribution {}", distributionId);

        return root;
    }
}
