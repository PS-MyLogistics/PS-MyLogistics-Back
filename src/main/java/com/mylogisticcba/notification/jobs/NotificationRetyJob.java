package com.mylogisticcba.notification.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylogisticcba.notification.repositories.NotificationRepository;
import com.mylogisticcba.notification.entity.NotificationEntity;
import com.mylogisticcba.notification.exceptions.NotificationException;
import com.mylogisticcba.notification.services.EmailNotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Component
public class NotificationRetyJob {

    private final NotificationRepository notificationRepository;
    private final EmailNotificationService emailNotificationService;
    private final ObjectMapper objectMapper;
    @Scheduled(fixedDelay = 300000) // Cada 5 minutos
    public void retryFailedNotifications() {

        List<NotificationEntity> failedNotifications =
                notificationRepository.findByStatus(NotificationEntity.NotificationStatus.FAILED);

        log.info("Encontradas {} notificaciones para reintentar", failedNotifications.size());

        for (NotificationEntity notification : failedNotifications) {
            try {
                if (notification.getStatus() == NotificationEntity.NotificationStatus.FAILED
                        && notification.getRetryCount()>=notification.getMaxRetry()){

                    notification.setStatus(NotificationEntity.NotificationStatus.CANCELLED);
                    notificationRepository.save(notification);
                }
                else {
                    retryNotification(notification);
                }


            } catch (Exception e) {
                log.error("Error reintentando notificación {}: {}",
                        notification.getId(), e.getMessage());
            }
        }
    }

    private void retryNotification(NotificationEntity notification) {
        try{

        log.info("Reintentando notificación {}", notification.getId());

        Map<String, Object> vars = objectMapper.readValue(notification.getVariables(),new TypeReference<Map<String, Object>>() {});
        Context context = new Context();
        vars.forEach(context::setVariable);

        emailNotificationService.sendEmail(
                notification.getTo(),
                notification.getSubject(),
                notification.getTemplateName(),
                context,
                notification.getTenantId());

        notification.setStatus(NotificationEntity.NotificationStatus.SENT);
        notification.setRetryCount(notification.getRetryCount() + 1);
        notificationRepository.save(notification);
        }
        catch (JsonProcessingException e) {
            notification.setStatus(NotificationEntity.NotificationStatus.CANCELLED);
            notification.setErrorMessage("Error convirtiendo variables del template a JSON"+e.getMessage());
            notificationRepository.save(notification);
        }
        catch (Exception e) {
            throw new NotificationException(e.getMessage());
        }

    }

}
