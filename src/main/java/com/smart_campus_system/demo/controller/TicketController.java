package com.smart_campus_system.demo.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smart_campus_system.demo.dto.AssignTechnicianRequest;
import com.smart_campus_system.demo.dto.CommentCreateRequest;
import com.smart_campus_system.demo.dto.TicketResponse;
import com.smart_campus_system.demo.dto.TicketStatusUpdateRequest;
import com.smart_campus_system.demo.dto.TicketSummaryResponse;
import com.smart_campus_system.demo.exception.CustomException;
import com.smart_campus_system.demo.model.TicketPriority;
import com.smart_campus_system.demo.service.TicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

	private final TicketService ticketService;

	public TicketController(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public TicketResponse create(
			@RequestParam String category,
			@RequestParam String description,
			@RequestParam String priority,
			@RequestParam String contactName,
			@RequestParam String contactEmail,
			@RequestParam String contactPhone,
			@RequestParam(value = "images", required = false) MultipartFile[] images,
			Authentication authentication) {
		List<MultipartFile> files = images == null
				? List.of()
				: Arrays.stream(images).filter(f -> f != null && !f.isEmpty()).toList();
		TicketPriority p;
		try {
			p = TicketPriority.valueOf(priority.trim().toUpperCase());
		}
		catch (Exception ex) {
			throw new CustomException("Invalid priority. Use LOW, MEDIUM, or HIGH");
		}
		return ticketService.create(category, description, p, contactName, contactEmail, contactPhone, files, authentication);
	}

	/** Full queue: staff only. Regular users should use {@link #listMine(Authentication)}. */
	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','TECHNICIAN')")
	public List<TicketSummaryResponse> list() {
		return ticketService.listAll();
	}

	@GetMapping("/me")
	public List<TicketSummaryResponse> listMine(Authentication authentication) {
		return ticketService.listMine(authentication);
	}

	@GetMapping("/{id}")
	public TicketResponse get(@PathVariable Long id) {
		return ticketService.get(id);
	}

	@PutMapping("/{id}/status")
	@PreAuthorize("hasAnyRole('ADMIN','TECHNICIAN')")
	public TicketResponse updateStatus(
			@PathVariable Long id,
			@Valid @RequestBody TicketStatusUpdateRequest body,
			Authentication authentication) {
		return ticketService.updateStatus(id, body, authentication);
	}

	@PutMapping("/{id}/assign")
	@PreAuthorize("hasRole('ADMIN')")
	public TicketResponse assign(
			@PathVariable Long id,
			@Valid @RequestBody AssignTechnicianRequest body) {
		return ticketService.assignTechnician(id, body);
	}

	@PostMapping("/{ticketId}/comments")
	public TicketResponse addComment(
			@PathVariable Long ticketId,
			@Valid @RequestBody CommentCreateRequest body,
			Authentication authentication) {
		return ticketService.addComment(ticketId, body, authentication);
	}

	@PutMapping("/{ticketId}/comments/{commentId}")
	@PreAuthorize("@commentSecurity.canModify(#commentId, authentication.name)")
	public TicketResponse updateComment(
			@PathVariable Long ticketId,
			@PathVariable Long commentId,
			@Valid @RequestBody CommentCreateRequest body,
			Authentication authentication) {
		return ticketService.updateComment(ticketId, commentId, body, authentication);
	}

	@DeleteMapping("/{ticketId}/comments/{commentId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@commentSecurity.canModify(#commentId, authentication.name)")
	public void deleteComment(
			@PathVariable Long ticketId,
			@PathVariable Long commentId,
			Authentication authentication) {
		ticketService.deleteComment(ticketId, commentId, authentication);
	}
}
