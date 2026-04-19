package com.smart_campus_system.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smart_campus_system.demo.model.Notification;
import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.model.Ticket;
import com.smart_campus_system.demo.model.TicketStatus;
import com.smart_campus_system.demo.model.User;
import com.smart_campus_system.demo.repository.NotificationRepository;
import com.smart_campus_system.demo.repository.UserRepository;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final ObjectProvider<JavaMailSender> mailSenderProvider;
	private final SseService sseService;

	@Value("${spring.mail.host:}")
	private String mailHost;

	public NotificationService(
			NotificationRepository notificationRepository,
			UserRepository userRepository,
			ObjectProvider<JavaMailSender> mailSenderProvider,
			SseService sseService) {
		this.notificationRepository = notificationRepository;
		this.userRepository = userRepository;
		this.mailSenderProvider = mailSenderProvider;
		this.sseService = sseService;
	}

	@Transactional
	public void notifyTicketStatusChanged(Ticket ticket, TicketStatus previous) {
		String body = "Ticket #" + ticket.getId() + " moved from " + previous + " to " + ticket.getStatus();
		String subject = "Ticket #" + ticket.getId() + " status update";
		if (ticket.getAssignedTechnician() != null) {
			saveInApp(ticket.getAssignedTechnician(), body, ticket.getId());
			sendEmail(ticket.getAssignedTechnician().getEmail(), subject, body);
		}
		for (User admin : userRepository.findByRole(Role.ADMIN)) {
			saveInApp(admin, body, ticket.getId());
		}
		sendEmail(ticket.getContactEmail(), subject, body);
	}

	@Transactional
	public void notifyTicketAssigned(Ticket ticket) {
		if (ticket.getAssignedTechnician() == null) {
			return;
		}
		String body = "You were assigned to ticket #" + ticket.getId() + " (" + ticket.getCategory() + ")";
		saveInApp(ticket.getAssignedTechnician(), body, ticket.getId());
		sendEmail(ticket.getAssignedTechnician().getEmail(), "New ticket assignment", body);
	}

	private void saveInApp(User user, String message, Long ticketId) {
		Notification n = Notification.builder()
				.userEmail(user.getEmail())
				.message(message)
				.isRead(false)
				.type("INFO")
				.ticketId(ticketId)
				.createdAt(java.time.LocalDateTime.now())
				.build();
		notificationRepository.save(n);
		// Also push real-time
		sseService.send(user.getEmail(), n);
	}

	public void saveNewCommentNotification(Ticket ticket, User commentAuthor, String recipientEmail) {
		String body = commentAuthor.getFirstName() + " commented on Ticket #" + ticket.getId()
				+ ": \"" + (ticket.getCategory() != null ? ticket.getCategory() : "") + "\"";
		Notification n = Notification.builder()
				.userEmail(recipientEmail)
				.message(body)
				.isRead(false)
				.type("INFO")
				.ticketId(ticket.getId())
				.createdAt(java.time.LocalDateTime.now())
				.build();
		notificationRepository.save(n);
		sseService.send(recipientEmail, n);
	}

	private void sendEmail(String to, String subject, String text) {
		if (to == null || to.isBlank()) {
			return;
		}
		if (mailHost == null || mailHost.isBlank()) {
			return;
		}
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (mailSender == null) {
			return;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(to);
			message.setSubject(subject);
			message.setText(text);
			mailSender.send(message);
		}
		catch (Exception ex) {
			log.warn("Could not send email to {}: {}", to, ex.getMessage());
		}
	}

	@Transactional
	public void process(Notification notification) {
		// Save to DB
		notificationRepository.save(notification);
		
		// Send real-time
		sseService.send(notification.getUserEmail(), notification);
	}
}
