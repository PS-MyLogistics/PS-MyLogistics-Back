package com.mylogisticcba.core.service.impl;

import com.mylogisticcba.core.dto.req.PromotionEmailRequest;
import com.mylogisticcba.core.dto.response.PromotionEmailResponse;
import com.mylogisticcba.core.entity.Customer;
import com.mylogisticcba.core.exceptions.OrderServiceException;
import com.mylogisticcba.core.repository.orders.CustomerRepository;
import com.mylogisticcba.core.service.PromotionService;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import com.mylogisticcba.notification.services.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.UUID;

@Service
public class PromotionServiceImpl implements PromotionService {

    private static final Logger log = LoggerFactory.getLogger(PromotionServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final EmailNotificationService emailNotificationService;

    public PromotionServiceImpl(CustomerRepository customerRepository,
                               EmailNotificationService emailNotificationService) {
        this.customerRepository = customerRepository;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public PromotionEmailResponse sendPromotionalEmail(PromotionEmailRequest request) {
        UUID tenantId = TenantContextHolder.getTenant();

        // Validar que se proporcionaron IDs de clientes
        if (request.getCustomerIds() == null || request.getCustomerIds().isEmpty()) {
            throw new OrderServiceException(
                "Debe seleccionar al menos un cliente para enviar el email promocional",
                HttpStatus.BAD_REQUEST
            );
        }

        // Obtener clientes del tenant
        List<Customer> customers = customerRepository.findByIdInAndTenantId(
            request.getCustomerIds(),
            tenantId
        );

        if (customers.isEmpty()) {
            throw new OrderServiceException(
                "No se encontraron clientes válidos con los IDs proporcionados",
                HttpStatus.BAD_REQUEST
            );
        }

        int sentCount = 0;
        int failedCount = 0;

        // Enviar emails a cada cliente
        for (Customer customer : customers) {
            try {
                if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
                    sendPromotionalEmailToCustomer(customer, request.getSubject(), request.getMessage(), tenantId);
                    sentCount++;
                    log.info("Promotional email sent to customer: {} ({})", customer.getName(), customer.getEmail());
                } else {
                    failedCount++;
                    log.warn("Customer {} does not have a valid email address", customer.getId());
                }
            } catch (Exception e) {
                failedCount++;
                log.error("Error sending promotional email to customer: {} - {}", customer.getId(), e.getMessage());
            }
        }

        boolean success = failedCount == 0;
        String message = success
            ? "Emails enviados exitosamente"
            : String.format("Se enviaron %d emails exitosamente y %d fallaron", sentCount, failedCount);

        log.info("Promotional email campaign completed for tenant: {} - Sent: {}, Failed: {}",
                 tenantId, sentCount, failedCount);

        return PromotionEmailResponse.builder()
            .success(success)
            .sentCount(sentCount)
            .failedCount(failedCount)
            .message(message)
            .build();
    }

    private void sendPromotionalEmailToCustomer(Customer customer, String subject, String message, UUID tenantId) {
        Context context = new Context();
        context.setVariable("customerName", customer.getName());
        context.setVariable("message", message);
        context.setVariable("subject", subject);

        emailNotificationService.sendEmail(
            customer.getEmail(),
            subject,
            "promotional-email",
            context,
            tenantId.toString()
        );
    }
}
