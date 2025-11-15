package com.mylogisticcba.notification.services.impl;

import com.mylogisticcba.core.entity.Customer;
import com.mylogisticcba.core.exceptions.OrderServiceException;
import com.mylogisticcba.core.repository.orders.CustomerRepository;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import com.mylogisticcba.notification.dto.DeliveryStartingRequest;
import com.mylogisticcba.notification.services.DeliveryNotificationService;
import com.mylogisticcba.notification.services.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryNotificationServiceImpl implements DeliveryNotificationService {

    private final CustomerRepository customerRepository;
    private final EmailNotificationService emailNotificationService;

    @Override
    public void sendDeliveryStartingNotification(DeliveryStartingRequest request) {
        UUID tenantId = TenantContextHolder.getTenant();
        if (tenantId == null) {
            throw new OrderServiceException("Invalid tenant context", HttpStatus.FORBIDDEN);
        }

        // Buscar el cliente
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new OrderServiceException(
                        String.format("No se encontró el cliente con ID '%s'.", request.getCustomerId()),
                        HttpStatus.NOT_FOUND
                ));

        // Validar que el cliente pertenezca al tenant
        if (!customer.getTenantId().equals(tenantId)) {
            throw new OrderServiceException(
                    "El cliente no pertenece a tu organización.",
                    HttpStatus.FORBIDDEN
            );
        }

        // Validar que el cliente tenga email
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new OrderServiceException(
                    String.format("El cliente '%s' no tiene un email configurado.", customer.getName()),
                    HttpStatus.BAD_REQUEST
            );
        }

        // Preparar el contexto para el template
        Context context = new Context();
        context.setVariable("customerName", customer.getName());
        context.setVariable("orderNumber", request.getOrderNumber());
        context.setVariable("estimatedPosition", request.getEstimatedPosition());
        context.setVariable("customerAddress", customer.getAddress());

        // Enviar el email
        try {
            emailNotificationService.sendEmail(
                    customer.getEmail(),
                    "Su pedido está en camino - Pedido #" + request.getOrderNumber(),
                    "delivery-starting",
                    context,
                    tenantId.toString()
            );
            log.info("Notificación de inicio de entrega enviada al cliente {} para el pedido {}",
                    customer.getId(), request.getOrderNumber());
        } catch (Exception e) {
            log.error("Error al enviar notificación de inicio de entrega: {}", e.getMessage(), e);
            throw new OrderServiceException(
                    "Error al enviar la notificación: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
