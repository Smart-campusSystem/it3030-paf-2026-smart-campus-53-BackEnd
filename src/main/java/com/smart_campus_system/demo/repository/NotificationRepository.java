package com.smart_campus_system.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart_campus_system.demo.model.Notification;
import com.smart_campus_system.demo.model.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByUserOrderByCreatedAtDesc(User user);
}
