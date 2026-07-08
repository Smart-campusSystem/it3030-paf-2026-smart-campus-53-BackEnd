package com.smart_campus_system.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.smart_campus_system.demo.model.Notification;

@Service
@ConditionalOnBean(RedisTemplate.class)
public class NotificationProducer {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void send(Notification notification) {
        redisTemplate.convertAndSend("notification-channel", notification);
    }
}
