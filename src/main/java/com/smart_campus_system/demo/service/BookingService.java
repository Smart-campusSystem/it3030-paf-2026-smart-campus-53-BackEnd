package com.smart_campus_system.demo.service;

import com.smart_campus_system.demo.config.AppConstants;
import com.smart_campus_system.demo.controller.BookingController;
import com.smart_campus_system.demo.dto.*;
import com.smart_campus_system.demo.exception.BadRequestException;
import com.smart_campus_system.demo.exception.ConflictException;
import com.smart_campus_system.demo.exception.ForbiddenException;
import com.smart_campus_system.demo.exception.ResourceNotFoundException;
import com.smart_campus_system.demo.model.*;
import com.smart_campus_system.demo.repository.BookingRepository;
import com.smart_campus_system.demo.repository.ResourceRepository;
import com.smart_campus_system.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private static final String DEV_EMAIL_DOMAIN = "@smartcampus.local";

    /**
     * Creates a new booking request for the given user email.
     */
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO dto, String userEmail) {
        log.info("Creating booking for user: {}", userEmail);

        Resource resource = resourceRepository.findById(dto.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            throw new BadRequestException("Resource is not ACTIVE");
        }

        if (dto.getStartTime() == null || dto.getEndTime() == null || !dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new BadRequestException("endTime must be after startTime");
        }

        if (!isWithinAvailabilityWindow(dto.getStartTime(), dto.getEndTime())) {
            throw new BadRequestException("Booking must be within resource availability window (06:00-22:00)");
        }

        boolean conflict = bookingRepository.existsConflictingBooking(resource.getId(), dto.getStartTime(), dto.getEndTime());
        if (conflict) {
            throw new ConflictException("Booking time conflicts with an existing approved booking");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseGet(() -> {
                    // UI-dev mode: if a request comes without a real authenticated user,
                    // allow booking to proceed by auto-provisioning a local demo user.
                    if (userEmail != null && userEmail.toLowerCase().endsWith(DEV_EMAIL_DOMAIN)) {
                        User demo = new User();
                        demo.setEmail(userEmail);
                        demo.setName("Demo User");
                        demo.setRole(UserRole.USER);
                        return userRepository.save(demo);
                    }
                    throw new ResourceNotFoundException("User not found");
                });

        Booking booking = new Booking();
        booking.setResource(resource);
        booking.setUser(user);
        booking.setStartTime(dto.getStartTime());
        booking.setEndTime(dto.getEndTime());
        booking.setPurpose(dto.getPurpose());
        booking.setExpectedAttendees(dto.getExpectedAttendees());
        booking.setStatus(BookingStatus.PENDING);
        booking.setRejectionReason(null);

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created with id: {}", saved.getId());

        BookingResponseDTO response = mapToResponse(saved, userEmail, user.getRole());
        response = addLinks(response, saved, userEmail, user.getRole() == UserRole.ADMIN);
        log.info("Creating booking for user: {} done", userEmail);
        return response;
    }

    /**
     * Returns bookings created by the current user.
     */
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getMyBookings(String userEmail) {
        log.info("Fetching bookings for user: {}", userEmail);
        List<Booking> bookings = bookingRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
        List<BookingResponseDTO> result = bookings.stream()
                .map(b -> addLinks(mapToResponse(b, userEmail, UserRole.USER), b, userEmail, false))
                .toList();
        log.info("Fetching bookings for user: {} done", userEmail);
        return result;
    }

    /**
     * Returns all bookings with optional status/date filters (admin).
     */
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getAllBookings(BookingStatus status, LocalDate date) {
        log.info("Admin fetching all bookings, filters: status={}, date={}", status, date);
        LocalDateTime startOfDay = null;
        LocalDateTime endOfDay = null;
        if (date != null) {
            startOfDay = date.atStartOfDay();
            endOfDay = date.plusDays(1).atStartOfDay();
        }
        List<Booking> bookings = bookingRepository.findAllWithFilters(status, date, startOfDay, endOfDay);
        List<BookingResponseDTO> result = bookings.stream()
                .map(b -> addLinks(mapToResponse(b, null, UserRole.ADMIN), b, null, true))
                .toList();
        log.info("Admin fetching all bookings done");
        return result;
    }

    /**
     * Returns a single booking by id, enforcing ownership for USER role.
     */
    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(Long id, String userEmail, UserRole userRole) {
        log.info("Fetching booking by id: {} for {}", id, userEmail);
        Booking booking = bookingRepository.findByIdWithJoins(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (userRole == UserRole.USER) {
            if (booking.getUser() == null || booking.getUser().getEmail() == null
                    || !booking.getUser().getEmail().equalsIgnoreCase(userEmail)) {
                throw new ForbiddenException("You do not have access to this booking");
            }
        }

        boolean isAdmin = userRole == UserRole.ADMIN;
        BookingResponseDTO result = addLinks(mapToResponse(booking, userEmail, userRole), booking, userEmail, isAdmin);
        log.info("Fetching booking by id: {} done", id);
        return result;
    }

    /**
     * Approves a pending booking (admin).
     */
    @Transactional
    public BookingResponseDTO approveBooking(Long id) {
        log.info("Approving booking id: {}", id);
        Booking booking = bookingRepository.findByIdWithJoins(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only PENDING bookings can be approved");
        }

        booking.setStatus(BookingStatus.APPROVED);
        booking.setRejectionReason(null);

        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} approved", id);
        return addLinks(mapToResponse(saved, null, UserRole.ADMIN), saved, null, true);
    }

    /**
     * Rejects a pending booking with a reason (admin).
     */
    @Transactional
    public BookingResponseDTO rejectBooking(Long id, String reason) {
        log.info("Rejecting booking id: {}", id);
        Booking booking = bookingRepository.findByIdWithJoins(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only PENDING bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectionReason(reason);

        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} rejected with reason: {}", id, reason);
        return addLinks(mapToResponse(saved, null, UserRole.ADMIN), saved, null, true);
    }

    /**
     * Cancels a booking owned by the current user.
     */
    @Transactional
    public BookingResponseDTO cancelBooking(Long id, String userEmail) {
        log.info("Cancelling booking id: {} by {}", id, userEmail);
        Booking booking = bookingRepository.findByIdWithJoins(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getUser() == null || booking.getUser().getEmail() == null
                || !booking.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new ForbiddenException("You can only cancel your own bookings");
        }

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.APPROVED) {
            throw new BadRequestException("Only PENDING or APPROVED bookings can be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} cancelled", id);
        return addLinks(mapToResponse(saved, userEmail, UserRole.USER), saved, userEmail, false);
    }

    /**
     * Checks availability for a resource in a time range.
     */
    @Transactional(readOnly = true)
    public AvailabilityResponseDTO checkAvailability(Long resourceId, LocalDateTime start, LocalDateTime end) {
        log.info("Checking availability for resource {} from {} to {}", resourceId, start, end);
        resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        AvailabilityResponseDTO dto = new AvailabilityResponseDTO();

        if (start == null || end == null || !end.isAfter(start)) {
            dto.setAvailable(false);
            dto.setMessage("Invalid time range");
            log.info("Checking availability for resource {} done", resourceId);
            return dto;
        }

        List<Booking> conflicts = bookingRepository.findConflictingBookings(resourceId, start, end);
        if (conflicts.isEmpty()) {
            dto.setAvailable(true);
            dto.setMessage("Available");
        } else {
            Booking first = conflicts.get(0);
            dto.setAvailable(false);
            dto.setConflictingBookingStart(first.getStartTime());
            dto.setConflictingBookingEnd(first.getEndTime());
            dto.setMessage("Not available due to a conflicting approved booking");
        }
        log.info("Checking availability for resource {} done", resourceId);
        return dto;
    }

    /**
     * Returns booking stats for admin dashboard.
     */
    @Transactional(readOnly = true)
    public BookingStatsDTO getBookingStats() {
        log.info("Fetching booking stats");
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        BookingStatsDTO dto = new BookingStatsDTO();
        dto.setTotalBookingsToday((int) bookingRepository.countCreatedBetween(startOfDay, endOfDay));
        dto.setPendingCount((int) bookingRepository.countByStatus(BookingStatus.PENDING));
        dto.setApprovedCount((int) bookingRepository.countByStatus(BookingStatus.APPROVED));
        dto.setRejectedCount((int) bookingRepository.countByStatus(BookingStatus.REJECTED));

        List<Object[]> mostBooked = bookingRepository.findMostBookedResource();
        if (mostBooked != null && !mostBooked.isEmpty()) {
            Object[] row = mostBooked.get(0);
            dto.setMostBookedResourceName(Objects.toString(row[0], null));
            dto.setMostBookedResourceCount(((Number) row[1]).intValue());
        } else {
            dto.setMostBookedResourceName(null);
            dto.setMostBookedResourceCount(0);
        }

        log.info("Fetching booking stats done");
        return dto;
    }

    private boolean isWithinAvailabilityWindow(LocalDateTime start, LocalDateTime end) {
        return !start.toLocalTime().isBefore(AppConstants.RESOURCE_AVAILABLE_FROM)
                && !end.toLocalTime().isAfter(AppConstants.RESOURCE_AVAILABLE_UNTIL);
    }

    private BookingResponseDTO mapToResponse(Booking booking, String requesterEmail, UserRole requesterRole) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());

        if (booking.getResource() != null) {
            dto.setResourceName(booking.getResource().getName());
            dto.setResourceLocation(booking.getResource().getLocation());
            dto.setResourceType(booking.getResource().getType() == null ? null : booking.getResource().getType().name());
            dto.setResourceCapacity(booking.getResource().getCapacity());
        }

        if (booking.getUser() != null) {
            dto.setUserName(booking.getUser().getName());
            dto.setUserEmail(booking.getUser().getEmail());
            dto.setUserProfilePicture(booking.getUser().getProfilePicture());
        }

        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setDurationMinutes(BookingResponseDTO.calculateDurationMinutes(booking.getStartTime(), booking.getEndTime()));

        dto.setPurpose(booking.getPurpose());
        dto.setExpectedAttendees(booking.getExpectedAttendees());

        dto.setStatus(booking.getStatus());
        dto.setRejectionReason(booking.getRejectionReason());

        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());

        boolean isOwner = requesterEmail != null
                && booking.getUser() != null
                && booking.getUser().getEmail() != null
                && booking.getUser().getEmail().equalsIgnoreCase(requesterEmail);

        dto.setCanCancel(isOwner && (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.APPROVED));
        dto.setCanApprove(requesterRole == UserRole.ADMIN && booking.getStatus() == BookingStatus.PENDING);

        return dto;
    }

    private BookingResponseDTO addLinks(BookingResponseDTO dto,
                                       Booking booking,
                                       String callerEmail,
                                       boolean isAdmin) {
        if (dto == null || booking == null || booking.getId() == null) return dto;

        dto.add(linkTo(methodOn(BookingController.class).getBookingById(booking.getId(), null)).withSelfRel());

        if (booking.getStatus() == BookingStatus.PENDING && isAdmin) {
            dto.add(linkTo(methodOn(BookingController.class).approve(booking.getId())).withRel("approve"));
            dto.add(linkTo(methodOn(BookingController.class).reject(booking.getId(), null)).withRel("reject"));
        }

        boolean isOwner = callerEmail != null
                && booking.getUser() != null
                && booking.getUser().getEmail() != null
                && booking.getUser().getEmail().equalsIgnoreCase(callerEmail);

        if ((booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.APPROVED) && isOwner) {
            dto.add(linkTo(methodOn(BookingController.class).cancel(booking.getId(), null)).withRel("cancel"));
        }

        if (isAdmin) {
            dto.add(linkTo(methodOn(BookingController.class).getAllBookings(null, null)).withRel("all-bookings"));
        } else {
            dto.add(linkTo(methodOn(BookingController.class).getMyBookings(null)).withRel("my-bookings"));
        }

        return dto;
    }
}
