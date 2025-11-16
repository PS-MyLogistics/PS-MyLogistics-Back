package com.mylogisticcba.notification.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylogisticcba.notification.repositories.NotificationRepository;
import com.mylogisticcba.notification.entity.NotificationEntity;
import com.mylogisticcba.notification.exceptions.NotificationException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class EmailNotificationService  implements com.mylogisticcba.notification.services.EmailNotificationService {

    @Value("${mail.username}") // ✅ AGREGAR ESTA LÍNEA
    private String username;

    private final NotificationRepository notificationRepository;
    private final Session mailSession;
    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    @Async("taskExecutor")
    public void sendEmail(String to, String subject, String templateName, Context context,String tenantID) {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Email destinatario no puede estar vacío");
                }

            // persist notification in DB with status PENDING
            NotificationEntity notificationSaved1 = createPendingNotification(to,subject,templateName,tenantID);
        try {
            // Validate format email
            new InternetAddress(to).validate();

            // Convert Context to String for persist in DB
            Map<String, Object> variablesMap = new HashMap<>();
            for (String varName : context.getVariableNames()) {
                Object value = context.getVariable(varName);
                variablesMap.put(varName, value);
            }

            String variablesJson = objectMapper.writeValueAsString(variablesMap);
            notificationSaved1.setVariables(variablesJson);

            // Create the content  HTML using Thymeleaf
            String body = templateEngine.process(templateName, context);

            // create msj
            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(username));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");

            // send email
            Transport.send(message);
            notificationSaved1.setStatus(NotificationEntity.NotificationStatus.SENT);
            notificationRepository.save(notificationSaved1);
            log.info("email enviado correctamente"+ notificationSaved1.getId());

        }
        catch (JsonProcessingException e)
        {
            notificationSaved1.setStatus(NotificationEntity.NotificationStatus.CANCELLED);
            notificationSaved1.setErrorMessage(e.getMessage());
            notificationRepository.save(notificationSaved1);
            log.error("Error serializing variables for email meessage:{} , notification: {}",e.getMessage(),notificationSaved1);
        }
        catch(MessagingException e) {
            notificationSaved1.setStatus(NotificationEntity.NotificationStatus.FAILED);
            notificationSaved1.setErrorMessage(e.getMessage());
            notificationRepository.save(notificationSaved1);
            log.warn("MessaginException : {} ,notification {}",e.getMessage(),notificationSaved1);
            throw new NotificationException("Error temporary sending email: " + e.getMessage(), HttpStatus.valueOf(503));


        } catch (Exception e) {
            notificationSaved1.setStatus(NotificationEntity.NotificationStatus.FAILED);
            notificationSaved1.setErrorMessage(e.getMessage());
            notificationRepository.save(notificationSaved1);
            log.warn("MessaginException : {} ,notification {}",e.getMessage(),notificationSaved1);
            throw new NotificationException("Error temporary sending email: " + e.getMessage(), HttpStatus.valueOf(503));

        }

    }

    private NotificationEntity createPendingNotification(String to, String subject, String templateName, String tenantID) {
        var notification = NotificationEntity.builder()
                .tenantId(tenantID)
                .to(to)
                .channel(NotificationEntity.NotificationChanelType.EMAIL)
                .subject(subject)
                .templateName(templateName)
                .status(NotificationEntity.NotificationStatus.PENDING)
                .variables("{}")
                .build();
        return notificationRepository.save(notification);
    }
}
