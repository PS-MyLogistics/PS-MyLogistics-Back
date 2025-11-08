package com.mylogisticcba.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylogisticcba.core.service.impl.OptimizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/optimization")
public class OptimizationRoutes {

    private final OptimizationService optimizationService;
    private final ObjectMapper objectMapper;

    public OptimizationRoutes(OptimizationService optimizationService, ObjectMapper objectMapper) {
        this.optimizationService = optimizationService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/routes/{distributionId}")
    public ResponseEntity<JsonNode> optimizeRoutes(@PathVariable UUID distributionId) {
        try {
            JsonNode node = optimizationService.optimizeRoutes(distributionId);
            return ResponseEntity.ok(node);
        } catch (IllegalArgumentException ex) {
            JsonNode err = objectMapper.createObjectNode().put("error", "bad_request").put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception ex) {
            JsonNode err = objectMapper.createObjectNode().put("error", "optimization_failed").put("message", ex.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }
}
