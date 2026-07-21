package com.fiapx.notification.infrastructure.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {}
