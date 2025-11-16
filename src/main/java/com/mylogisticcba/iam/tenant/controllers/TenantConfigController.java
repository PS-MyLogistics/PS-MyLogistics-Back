package com.mylogisticcba.iam.tenant.controllers;

import com.mylogisticcba.iam.tenant.services.impl.TenantConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/config")
public class TenantConfigController {

    private final TenantConfigService service;

    public TenantConfigController(TenantConfigService service) {
        this.service = service;
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<Map<String,String>> getConfig(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(service.getConfigForTenant(tenantId));
    }
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("controller test");
    }
}
