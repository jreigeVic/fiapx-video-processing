package com.fiapx.notification.application.ports.out;

import com.fiapx.notification.domain.model.Notification;

public interface NotificationRepositoryPort {

    void save(Notification notification);
}
