package com.smart_campus_system.demo.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.smart_campus_system.demo.dto.AssignTechnicianRequest;
import com.smart_campus_system.demo.dto.CommentCreateRequest;
import com.smart_campus_system.demo.dto.TicketResponse;
import com.smart_campus_system.demo.dto.TicketStatusUpdateRequest;
import com.smart_campus_system.demo.dto.TicketSummaryResponse;
import com.smart_campus_system.demo.exception.CustomException;
import com.smart_campus_system.demo.exception.NotFoundException;
import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.model.Ticket;
import com.smart_campus_system.demo.model.TicketComment;
import com.smart_campus_system.demo.model.TicketPriority;
import com.smart_campus_system.demo.model.TicketStatus;
import com.smart_campus_system.demo.model.User;
import com.smart_campus_system.demo.repository.TicketCommentRepository;
import com.smart_campus_system.demo.repository.TicketRepository;
import com.smart_campus_system.demo.repository.UserRepository;

@Service
public class TicketService {

	private final TicketRepository ticketRepository;
	private final UserRepository userRepository;
	private final TicketCommentRepository commentRepository;
	private final FileStorageService fileStorageService;
	private final NotificationService notificationService;

	public TicketService(
			TicketRepository ticketRepository,
			UserRepository userRepository,
			TicketCommentRepository commentRepository,
			FileStorageService fileStorageService,
			NotificationService notificationService) {
		this.ticketRepository = ticketRepository;
		this.userRepository = userRepository;
		this.commentRepository = commentRepository;
		this.fileStorageService = fileStorageService;
		this.notificationService = notificationService;
	}

	@Transactional
	public TicketResponse create(
			String category,
			String description,
			TicketPriority priority,
			String contactName,
			String contactEmail,
			String contactPhone,
			List<MultipartFile> images,
			Authentication authentication) {
		Ticket ticket = new Ticket();
		ticket.setCategory(category);
		ticket.setDescription(description);
		ticket.setPriority(priority != null ? priority : TicketPriority.MEDIUM);
		ticket.setContactName(contactName);
		ticket.setContactEmail(contactEmail);
		ticket.setContactPhone(contactPhone);
		ticket.setStatus(TicketStatus.OPEN);
		if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
			userRepository.findByEmail(authentication.getName()).ifPresent(ticket::setSubmitter);
		}
		ticketRepository.save(ticket);
		var attachments = fileStorageService.saveImages(ticket, images);
		for (var a : attachments) {
			ticket.getAttachments().add(a);
		}
		ticketRepository.save(ticket);
		return loadTicketResponse(ticket.getId());
	}

	@Transactional(readOnly = true)
	public TicketResponse get(Long id) {
		return loadTicketResponse(id);
	}

	@Transactional(readOnly = true)
	public List<TicketSummaryResponse> listAll() {
		return ticketRepository.findAll().stream()
				.sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
				.map(TicketSummaryResponse::fromEntity)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<TicketSummaryResponse> listMine(Authentication authentication) {
		User user = userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));
		return ticketRepository.findMine(user.getId(), user.getEmail()).stream()
				.map(TicketSummaryResponse::fromEntity)
				.toList();
	}

	@Transactional
	public TicketResponse updateStatus(Long id, TicketStatusUpdateRequest request, Authentication authentication) {
		Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
		User actor = userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));
		TicketStatus previous = ticket.getStatus();
		TicketStatus next = request.getStatus();
		if (!isTransitionAllowed(previous, next, actor.getRole())) {
			throw new CustomException("Status transition from " + previous + " to " + next + " is not allowed");
		}
		ticket.setStatus(next);
		ticketRepository.save(ticket);
		if (previous != next) {
			notificationService.notifyTicketStatusChanged(ticket, previous);
		}
		return loadTicketResponse(id);
	}

	@Transactional
	public TicketResponse assignTechnician(Long id, AssignTechnicianRequest request) {
		Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
		User tech = userRepository.findById(request.getTechnicianId())
				.orElseThrow(() -> new NotFoundException("Technician not found"));
		if (tech.getRole() != Role.TECHNICIAN) {
			throw new CustomException("Selected user is not a technician");
		}
		ticket.setAssignedTechnician(tech);
		ticketRepository.save(ticket);
		notificationService.notifyTicketAssigned(ticket);
		return loadTicketResponse(id);
	}

	@Transactional
	public TicketResponse addComment(Long ticketId, CommentCreateRequest request, Authentication authentication) {
		Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));
		User author = userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));
		TicketComment comment = new TicketComment();
		comment.setTicket(ticket);
		comment.setAuthor(author);
		comment.setBody(request.getText());
		ticket.getComments().add(comment);
		commentRepository.save(comment);
		return loadTicketResponse(ticketId);
	}

	@Transactional
	public TicketResponse updateComment(Long ticketId, Long commentId, CommentCreateRequest request, Authentication authentication) {
		ensureTicketMatch(ticketId, commentId);
		TicketComment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found"));
		assertAuthorOrAdmin(comment, authentication.getName());
		comment.setBody(request.getText());
		comment.setUpdatedAt(java.time.Instant.now());
		commentRepository.save(comment);
		return loadTicketResponse(ticketId);
	}

	@Transactional
	public void deleteComment(Long ticketId, Long commentId, Authentication authentication) {
		ensureTicketMatch(ticketId, commentId);
		TicketComment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found"));
		assertAuthorOrAdmin(comment, authentication.getName());
		commentRepository.delete(comment);
	}

	private void ensureTicketMatch(Long ticketId, Long commentId) {
		TicketComment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found"));
		if (!comment.getTicket().getId().equals(ticketId)) {
			throw new NotFoundException("Comment not found");
		}
	}

	private void assertAuthorOrAdmin(TicketComment comment, String username) {
		User user = userRepository.findByEmail(username).orElseThrow(() -> new NotFoundException("User not found"));
		if (user.getRole() == Role.ADMIN) {
			return;
		}
		if (!comment.getAuthor().getId().equals(user.getId())) {
			throw new CustomException("You can only modify your own comments");
		}
	}

	private static boolean isTransitionAllowed(TicketStatus from, TicketStatus to, Role role) {
		if (from == to) {
			return true;
		}
		if (role == Role.ADMIN) {
			return true;
		}
		if (role != Role.TECHNICIAN) {
			return false;
		}
		return switch (from) {
			case OPEN -> to == TicketStatus.IN_PROGRESS || to == TicketStatus.REJECTED;
			case IN_PROGRESS -> to == TicketStatus.RESOLVED || to == TicketStatus.OPEN;
			case RESOLVED -> to == TicketStatus.CLOSED;
			default -> false;
		};
	}

	private TicketResponse loadTicketResponse(Long id) {
		Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
		ticket.getAttachments().size();
		ticket.getComments().size();
		for (TicketComment c : ticket.getComments()) {
			c.getAuthor().getEmail();
		}
		if (ticket.getAssignedTechnician() != null) {
			ticket.getAssignedTechnician().getEmail();
		}
		return TicketResponse.fromEntity(ticket);
	}
}
