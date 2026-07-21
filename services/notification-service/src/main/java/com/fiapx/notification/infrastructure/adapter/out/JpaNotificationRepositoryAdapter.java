package com.fiapx.notification.infrastructure.adapter.out;

import com.fiapx.notification.application.ports.out.NotificationRepositoryPort;
import com.fiapx.notification.domain.model.Notification;
import com.fiapx.notification.infrastructure.repository.NotificationJpaEntity;
import com.fiapx.notification.infrastructure.repository.NotificationJpaRepository;

public class JpaNotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final NotificationJpaRepository jpaRepository;

    public JpaNotificationRepositoryAdapter(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Notification notification) {
        jpaRepository.save(
                new NotificationJpaEntity(
                        notification.getId(),
                        notification.getVideoId(),
                        notification.getOwnerUserId(),
                        notification.getType().name(),
                        notification.getStatus().name(),
                        notification.getCreatedAt(),
                        notification.getSentAt()));
    }
}
