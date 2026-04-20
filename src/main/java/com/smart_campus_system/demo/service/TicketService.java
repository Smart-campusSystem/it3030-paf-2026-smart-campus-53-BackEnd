package com.smart_campus_system.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.smart_campus_system.demo.dto.AssignTechnicianRequest;
import com.smart_campus_system.demo.dto.CommentCreateRequest;
import com.smart_campus_system.demo.dto.TicketResponse;
import com.smart_campus_system.demo.dto.TicketStatusUpdateRequest;
import com.smart_campus_system.demo.dto.TicketSummaryResponse;
import com.smart_campus_system.demo.exception.ApiException;
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
import com.smart_campus_system.demo.security.UserPrincipal;

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
		tryResolveUser(authentication).ifPresent(ticket::setSubmitter);
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
		return ticketRepository.findAllByOrderByCreatedAtDesc().stream()
				.map(TicketSummaryResponse::fromEntity)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<TicketSummaryResponse> listMine(Authentication authentication) {
		User user = requireSignedInUser(authentication);
		return ticketRepository.findMine(user.getId(), user.getEmail()).stream()
				.map(TicketSummaryResponse::fromEntity)
				.toList();
	}

	@Transactional
	public TicketResponse updateStatus(Long id, TicketStatusUpdateRequest request, Authentication authentication) {
		Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
		User actor = requireSignedInUser(authentication);
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
		User author = requireSignedInUser(authentication);
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
		User actor = requireSignedInUser(authentication);
		assertAuthorOrAdmin(comment, actor.getEmail());
		comment.setBody(request.getText());
		comment.setUpdatedAt(java.time.Instant.now());
		commentRepository.save(comment);
		return loadTicketResponse(ticketId);
	}

	@Transactional
	public void deleteComment(Long ticketId, Long commentId, Authentication authentication) {
		ensureTicketMatch(ticketId, commentId);
		TicketComment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found"));
		User actor = requireSignedInUser(authentication);
		assertAuthorOrAdmin(comment, actor.getEmail());
		commentRepository.delete(comment);
	}

	/**
	 * Admins may delete any ticket. Other signed-in users may delete only their own ticket while it is still {@link TicketStatus#OPEN}.
	 */
	@Transactional
	public void deleteTicket(Long id, Authentication authentication) {
		Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
		User actor = requireSignedInUser(authentication);
		boolean admin = actor.getRole() == Role.ADMIN;
		boolean submitterMatch = ticket.getSubmitter() != null && ticket.getSubmitter().getId().equals(actor.getId());
		boolean legacyReporter = ticket.getSubmitter() == null
				&& ticket.getContactEmail() != null
				&& actor.getEmail() != null
				&& ticket.getContactEmail().trim().equalsIgnoreCase(actor.getEmail().trim());
		if (!admin && !submitterMatch && !legacyReporter) {
			throw new ApiException(HttpStatus.FORBIDDEN, "You can only delete tickets you submitted");
		}
		if (!admin && ticket.getStatus() != TicketStatus.OPEN) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Only open tickets can be deleted. Contact staff if you need this ticket closed.");
		}
		ticketRepository.delete(ticket);
	}

	private void ensureTicketMatch(Long ticketId, Long commentId) {
		TicketComment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found"));
		if (!comment.getTicket().getId().equals(ticketId)) {
			throw new NotFoundException("Comment not found");
		}
	}

	private void assertAuthorOrAdmin(TicketComment comment, String actorEmail) {
		User user = userRepository.findByEmail(actorEmail).orElseThrow(() -> new NotFoundException("User not found"));
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

	/**
	 * Resolves the campus {@link User} from JWT ({@link UserPrincipal}), OAuth2 login, or standard
	 * {@link UserDetails#getUsername()} (tests / form login). {@link Authentication#getName()} alone is not reliable
	 * for OAuth2 principals.
	 */
	private Optional<User> tryResolveUser(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return Optional.empty();
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof UserPrincipal up) {
			return userRepository.findById(up.id());
		}
		if (principal instanceof OAuth2User ou) {
			String email = ou.getAttribute("email");
			if (email != null && !email.isBlank()) {
				return userRepository.findByEmail(email.trim());
			}
		}
		if (principal instanceof UserDetails ud) {
			String username = ud.getUsername();
			if (username != null && !username.isBlank() && !"anonymousUser".equals(username)) {
				return userRepository.findByEmail(username);
			}
		}
		String name = authentication.getName();
		if (name != null && !name.isBlank() && !"anonymousUser".equals(name)) {
			return userRepository.findByEmail(name);
		}
		return Optional.empty();
	}

	private User requireSignedInUser(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Sign in required");
		}
		return tryResolveUser(authentication).orElseThrow(() -> new NotFoundException("User not found"));
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
		if (ticket.getSubmitter() != null) {
			ticket.getSubmitter().getEmail();
		}
		return TicketResponse.fromEntity(ticket);
	}
}
