package com.mylogisticcba.notification.repositories;

import com.mylogisticcba.notification.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {

    // for retry mechanism
    List<NotificationEntity> findByStatus(
            NotificationEntity.NotificationStatus status
    );

    List<NotificationEntity> findByTenantIdAndStatus(
            String tenantId,
            NotificationEntity.NotificationStatus status
    );

    List<NotificationEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);}
