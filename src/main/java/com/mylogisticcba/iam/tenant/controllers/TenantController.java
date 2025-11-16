package com.mylogisticcba.iam.tenant.controllers;
import com.mylogisticcba.iam.tenant.dtos.TenantInfo;
import com.mylogisticcba.iam.tenant.services.impl.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tenant/")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService service;

    @GetMapping("/info")
    public ResponseEntity<TenantInfo> getInfo() {

        return ResponseEntity.ok(service.getInfo());
    }

}
