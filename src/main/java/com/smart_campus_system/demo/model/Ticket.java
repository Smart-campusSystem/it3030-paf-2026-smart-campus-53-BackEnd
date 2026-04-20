package com.smart_campus_system.demo.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String category;

	@Column(nullable = false, length = 4000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TicketPriority priority = TicketPriority.MEDIUM;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TicketStatus status = TicketStatus.OPEN;

	@Column(nullable = false)
	private String contactName;

	@Column(nullable = false)
	private String contactEmail;

	@Column(nullable = false)
	private String contactPhone;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_technician_id")
	private User assignedTechnician;

	/** Logged-in user who submitted the ticket (optional for legacy rows). */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "submitter_id")
	private User submitter;

	@Column(nullable = false)
	private Instant createdAt = Instant.now();

	@OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TicketAttachment> attachments = new ArrayList<>();

	@OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("createdAt ASC")
	private List<TicketComment> comments = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TicketPriority getPriority() {
		return priority;
	}

	public void setPriority(TicketPriority priority) {
		this.priority = priority;
	}

	public TicketStatus getStatus() {
		return status;
	}

	public void setStatus(TicketStatus status) {
		this.status = status;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public User getAssignedTechnician() {
		return assignedTechnician;
	}

	public void setAssignedTechnician(User assignedTechnician) {
		this.assignedTechnician = assignedTechnician;
	}

	public User getSubmitter() {
		return submitter;
	}

	public void setSubmitter(User submitter) {
		this.submitter = submitter;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public List<TicketAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<TicketAttachment> attachments) {
		this.attachments = attachments;
	}

	public List<TicketComment> getComments() {
		return comments;
	}

	public void setComments(List<TicketComment> comments) {
		this.comments = comments;
	}
}
