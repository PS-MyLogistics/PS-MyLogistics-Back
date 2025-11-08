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

@Service
public class OptimizationService {

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

        // Construir jobs a partir de orders -> customer lat/lon
        List<Map<String, Object>> jobs = new ArrayList<>();
        Map<Integer, UUID> jobIdToOrderId = new HashMap<>();
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

            Map<String, Object> job = new HashMap<>();
            job.put("id", jobId);
            job.put("location", Arrays.asList(lon, lat)); // ORS espera [lon, lat]
            job.put("service", 0);
            Map<String, Object> props = new HashMap<>();
            props.put("orderId", o.getId().toString());
            job.put("properties", props);

            jobs.add(job);
            jobIdToOrderId.put(jobId, o.getId());
            jobId++;
        }

        // vehicles: por defecto 1, usando la ubicación del primer job como start/end
        List<Map<String, Object>> vehicles = new ArrayList<>();
        List<Double> startCoord = (List<Double>) jobs.get(0).get("location");
        Map<String, Object> vehicle = new HashMap<>();
        vehicle.put("id", 1);
        vehicle.put("profile", "driving-car");
        vehicle.put("start", startCoord);
        vehicle.put("end", startCoord);
        vehicle.put("capacity", Arrays.asList(100));
        vehicles.add(vehicle);

        Map<String, Object> body = new HashMap<>();
        body.put("jobs", jobs);
        body.put("vehicles", vehicles);

        Map<String, String> headers = Map.of("Authorization", ORS_API_KEY, "Content-Type", "application/json");

        String resp = restClientService.postWithHeaders(ORS_OPTIMIZATION_URL, body, headers, String.class);
        JsonNode root = objectMapper.readTree(resp);

        // Extraer orden optimizada: buscar routes -> steps -> tipo job
        List<Integer> optimizedJobOrder = new ArrayList<>();
        if (root.has("routes")) {
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

        // Mapear job ids a order UUIDs
        List<UUID> optimizedOrderIds = new ArrayList<>();
        for (Integer jid : optimizedJobOrder) {
            UUID oid = jobIdToOrderId.get(jid);
            if (oid != null) optimizedOrderIds.add(oid);
        }

        // Guardar secuencia optimizada en la distribution

            distributionService.saveOptimizedSequence(distributionId, optimizedOrderIds);


        return root;
    }
}
