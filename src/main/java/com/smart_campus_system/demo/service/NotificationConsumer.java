package com.smart_campus_system.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import com.smart_campus_system.demo.model.Notification;

@Service
@ConditionalOnBean(RedisConnectionFactory.class)
public class NotificationConsumer {

    @Autowired
    private NotificationService notificationService;

    // Note: Spring Data Redis uses MessageListenerAdapter instead of a @RedisListener annotation.
    // This method will be bound to the "notification-channel" topic in our RedisConfig.
    public void consume(Notification notification) {
        notificationService.process(notification);
    }
}
