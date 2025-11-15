package com.mylogisticcba.notification.services;

import org.thymeleaf.context.Context;

public interface EmailNotificationService {
        void sendEmail(String to, String subject, String templateName, Context context, String tenantID);

    }
