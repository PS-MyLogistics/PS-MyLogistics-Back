package com.mylogisticcba.notification.controller;

import com.mylogisticcba.notification.dto.DeliveryStartingRequest;
import com.mylogisticcba.notification.services.DeliveryNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final DeliveryNotificationService deliveryNotificationService;

    @PreAuthorize("hasAnyRole('ADMIN','OWNER','DEALER')")
    @PostMapping("/delivery-starting")
    public ResponseEntity<Map<String, String>> sendDeliveryStartingNotification(
            @Valid @RequestBody DeliveryStartingRequest request) {

        deliveryNotificationService.sendDeliveryStartingNotification(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notificación enviada correctamente");
        return ResponseEntity.ok(response);
    }
}
