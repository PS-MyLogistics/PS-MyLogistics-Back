package com.mylogisticcba.notification.listeners;

import com.mylogisticcba.iam.events.*;
import com.mylogisticcba.notification.services.EmailNotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.context.Context;

import java.time.Instant;

@Component
@AllArgsConstructor
@Slf4j
public class NotificationEventListener {
    private final EmailNotificationService emailNotificationService;

    //listener para registros de tenant y usuario
    @EventListener
    public void onCreatedTenant(TenantCreatedEvent event) {
        // build dinamic URL
        String verificationUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/auth/verifyRegisterTenantAndOwner")
                .queryParam("token", event.getTokenVerification())
                .queryParam("idTenant", event.getTenantId())
                .build()
                .toString();
        log.info("Verification URL in notificationeventListener: " + verificationUrl);
        Context context = new Context();
        context.setVariable("nombre", event.getOwnerUsername());
        context.setVariable("verificationUrl", verificationUrl);

        // Enviar email al tenant
        emailNotificationService.sendEmail(
                event.getTenantEmail(),
                "Verifica tu cuenta para dar de alta a tu espacio de trabajo",
                "register-verification-email-template",
                context,
                event.getTenantId().toString()
        );

        // Solo enviar email al owner si es diferente al tenant
        if (!event.getTenantEmail().equals(event.getOwnerEmail())) {
            emailNotificationService.sendEmail(
                    event.getOwnerEmail(),
                    "Verifica tu cuenta para dar de alta a tu espacio de trabajo",
                    "register-verification-email-template",
                    context,
                    event.getTenantId().toString()
            );
        }

    }

    @EventListener
    public void  onCreatedUser(UserInTenantCreatedEvent event) {
        // build dinamic URL
        String verificationUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/auth/verifyRegisterUserInTenant")
                .queryParam("token", event.getTokenVerification())
                .queryParam("idTenant", event.getTenantId())
                .build()
                .toString();
        log.info("Verification URL in notificationeventListener: " + verificationUrl);
        Context context = new Context();
        context.setVariable("nameTenant", event.getTenantName());
        context.setVariable("name", event.getUsername());
        context.setVariable("verificationUrl", verificationUrl);
        emailNotificationService.sendEmail(
                event.getEmail(),
                "Verifica tu cuenta ",
                "register-verification-email-template2",
                context,
                event.getTenantId().toString()
        );
    }

    @EventListener
    public void  onCreatedResetPasswordToken(ResetPasswordTokenCreatedEvent event) {

        log.info("✅Listener created Reset-Password check User:  " + event.getUsername()+",Tenant:"+ event.getTenantName());
        Context context = new Context();
        context.setVariable("nameTenant", event.getTenantName());
        context.setVariable("user", event.getUsername());
        context.setVariable("token", event.getToken());
        //TODO: modify the template the central box is not visible
        emailNotificationService.sendEmail(
                event.getEmail(),
                "Reset Password Code",
                "reset-password-token-email-template",
                context,
                event.getTenantId().toString()
        );
    }

    @EventListener
    public void  onConfirmResetPasswordToken(ResetPasswordConfirmEvent event) {

        log.info("✅Listener confirm Reset-Password ok User:  " + event.getUsername()+",Tenant:"+ event.getTenantName());
        Context context = new Context();
        context.setVariable("nameTenant", event.getTenantName());
        context.setVariable("user", event.getUsername());
        emailNotificationService.sendEmail(
                event.getEmail(),
                "Reset password ok",
                "reset-password-confirm-email-template",
                context,
                event.getTenantId().toString()
        );
    }

    //listener para rateLimits

    @EventListener
    public void onFailLoginExceedLimit(FailLoginExceededEvent event){

        Context context = new Context();
        context.setVariable("tenantName", event.getTenantName());
        context.setVariable("username", event.getUsername());
        context.setVariable("attemptCount", event.getAttemptCount());
        context.setVariable("email", event.getEmail());
        context.setVariable("lastAttemptTime",event.getLastAttemptTime());
        //TODO: modify the template the central box is not visible
        emailNotificationService.sendEmail(
                event.getEmail(),
                "Temporal Lockout",
                "rate-limit-exceeded-login",
                context,
                event.getTenantId().toString()
        );

        log.info("✅Listener rate limit login  onFailLoginExceedLimit :  " + event.getUsername()+",Tenant:"+ event.getTenantName());


    }
    @EventListener
    public void onMaxDailyFailLoginExceedLimit(DailyFailLoginExceededEvent event){
        // build dinamic URL
        String verificationUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/auth/unfreeze")
                .queryParam("token", event.getToken())
                .queryParam("tenantName", event.getTenantName())
                .queryParam("username", event.getUsername())
                .build()
                .toString();
        log.info("Verification URL in notificationeventListener: " + verificationUrl);
        Context context = new Context();
        context.setVariable("tenantName", event.getTenantName());
        context.setVariable("username", event.getUsername());
        context.setVariable("attemptCount", event.getAttemptCount());
        context.setVariable("lockouts", event.getLockouts());
        context.setVariable("lastAttemptTime",event.getLastAttemptTime());
        context.setVariable("token",event.getToken());
        context.setVariable("email", event.getEmail());
        context.setVariable("url",verificationUrl);
        emailNotificationService.sendEmail(
                event.getEmail(),
                "Reset Password Code",
                "rate-limit-login-daily-lockout-exceeded",
                context,
                event.getTenantId().toString()
        );

        log.info("✅Listener rate limit login  onMaxDailyFailLoginExceedLimit :  " + event.getUsername()+",Tenant:"+ event.getTenantName());


    }




    //TODO: verificar si realmente hace falta notificar al usuario por ataque a ese endpointe auth/unfrezze
    /*
    @EventListener
    public void onConfirmResetPasswordExceededLimit(ResetPasswordConfirmLimitRequestExceededEvent event){
        Context context = new Context();
        context.setVariable("nameTenant", event.getTenantName());
        context.setVariable("name", event.getUsername());
        emailNotificationService.sendEmail(
                event.getEmail(),
                "Aviso de seguridad",

                ,
                context,
                event.getTenantId().toString()
        );
        log.info("notification for confirm reset password exceeded request OK");

    }*/

    @EventListener
    public void onRequestResetPasswordExceededLimit(ResetPasswordRequestTokenExceededEvent event){
        Context context = new Context();
        context.setVariable("tenantName", event.getTenantName());
        context.setVariable("username", event.getUsername());
        context.setVariable("timestamp", Instant.now());
        context.setVariable("attemptCount", event);
        emailNotificationService.sendEmail(
                event.getEmail(),
                "Aviso de seguridad",
                "rate-limit-exceeded-request-reset-password"
                ,
                context,
                event.getTenantId().toString()
        );
        log.info("notification for request reset password exceeded  OK");

    }





}
