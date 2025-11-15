package com.mylogisticcba.notification.services;

import com.mylogisticcba.notification.dto.DeliveryStartingRequest;

public interface DeliveryNotificationService {
    void sendDeliveryStartingNotification(DeliveryStartingRequest request);
}
