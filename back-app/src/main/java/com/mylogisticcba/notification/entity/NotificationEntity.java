package com.mylogisticcba.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String tenantId;
    @Column(name="destination")
    private String to;
    @Enumerated(EnumType.STRING)
    private NotificationChanelType channel;   // email, sms, push
    private String subject;                   // opc, x mails
    private String templateName ;             // opc, x mails
    private LocalDateTime createdAt;
    private String variables;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    private String errorMessage;                 // if  failed status

    private int retryCount = 0;                 // number of retry attempts
    private int maxRetry = 3;                   // max number of retry attempts

    public enum NotificationChanelType {
        EMAIL,
        SMS,
        PUSH
    }

    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED,
        CANCELLED   // more retry than max retrys
    }
    @PrePersist
    public void prePersist() {

        this.id = UUID.randomUUID();

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

    }
}

