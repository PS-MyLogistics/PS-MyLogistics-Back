package com.mylogisticcba.core.controller;

import com.mylogisticcba.core.dto.req.PromotionEmailRequest;
import com.mylogisticcba.core.dto.response.PromotionEmailResponse;
import com.mylogisticcba.core.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    /**
     * Envía un email promocional a una lista de clientes
     * @param request Datos del email promocional (IDs de clientes, asunto, mensaje)
     * @return Resultado del envío (cantidad exitosos y fallidos)
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @PostMapping("/send-email")
    public ResponseEntity<PromotionEmailResponse> sendPromotionalEmail(
            @Valid @RequestBody PromotionEmailRequest request) {
        PromotionEmailResponse response = promotionService.sendPromotionalEmail(request);
        return ResponseEntity.ok(response);
    }
}
