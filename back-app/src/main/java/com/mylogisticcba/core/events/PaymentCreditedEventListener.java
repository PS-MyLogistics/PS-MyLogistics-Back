package com.mylogisticcba.core.events;

import com.mylogisticcba.iam.tenant.entity.TenantEntity;
import com.mylogisticcba.iam.tenant.enums.PlanType;
import com.mylogisticcba.iam.tenant.services.TenantService;
import com.mylogisticcba.payments.mercadoPago.event.PaymentCreditedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCreditedEventListener {

    private final TenantService tenantService;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePaymentCredited(PaymentCreditedEvent event) {
        try {
            log.info("🔔 Recibido PaymentCreditedEvent para tenant={} factura={}", event.getTenantId(), event.getFacturaId());

            LocalDateTime paymentAt = event.getPaymentAt() != null ? event.getPaymentAt() : LocalDateTime.now();

            // Determinar meses: usar months del evento si está presente y válido (1,3,6), sino fallback a 1
            Integer eventMonths = event.getMonths();
            int monthsToAdd = 1; // default
            if (eventMonths != null && (eventMonths == 1 || eventMonths == 3 || eventMonths == 6)) {
                monthsToAdd = eventMonths;
            }

            LocalDateTime newEndDate = paymentAt.plusMonths(monthsToAdd);

            // Obtener tenant, setear plan y fecha, y persistir
            TenantEntity tenant = tenantService.getTenantById(event.getTenantId());

            log.info("🔧 Modificando tenant en memoria antes de persistir. planActual={} maxUsersActual={}", tenant.getPlanType(), tenant.getMaxUsers());

            tenant.setPlanType(PlanType.PREMIUM);
            tenant.setEndDatePremium(newEndDate);
            tenant.setUpdatedAt(LocalDateTime.now());
            tenant.setMaxUsers(75); // actualizar max users para plan PREMIUM

            // Llamar al servicio para que haga el save dentro de la NUEVA transacción
            TenantEntity saved = tenantService.updateTenant(tenant);

            log.info("✅ Tenant {} actualizado a PREMIUM hasta {} (meses={}), planGuardado={}", saved.getId(), newEndDate, monthsToAdd, saved.getPlanType());

        } catch (Exception e) {
            log.error("❌ Error al procesar PaymentCreditedEvent", e);
            // No re-lanzamos para no romper el procesamiento de eventos
        }
    }
}
