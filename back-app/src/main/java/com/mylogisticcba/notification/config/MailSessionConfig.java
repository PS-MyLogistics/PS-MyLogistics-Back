package com.mylogisticcba.notification.config;


import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

    @Configuration
    @RequiredArgsConstructor
    public class MailSessionConfig {

        @Value("${mail.username}")
        private String username;

        @Value("${mail.password}")
        private String password;

        @Value("${mail.host}")
        private String host;

        @Value("${mail.port}")
        private String port;

        @Bean
        public Session mailSession() {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        }
    }

