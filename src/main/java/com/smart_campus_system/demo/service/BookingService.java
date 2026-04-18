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
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
    private final QrCodeService qrCodeService;
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

        if (bookingRepository.existsConflictingBooking(resource.getId(), dto.getStartTime(), dto.getEndTime(), null)) {
            throw new ConflictException("Resource is already booked for the requested time range");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseGet(() -> {
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
        booking.setCancellationReason(null);
        booking.setQrPayload(null);

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created with id: {}", saved.getId());

        BookingResponseDTO response = mapToResponse(saved, userEmail, user.getRole());
        response = addLinks(response, saved, userEmail, user.getRole() == UserRole.ADMIN);
        log.info("Creating booking for user: {} done", userEmail);
        return response;
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> listBookings(String userEmail,
                                                 UserRole role,
                                                 BookingStatus status,
                                                 LocalDate from,
                                                 LocalDate to,
                                                 LocalDate singleDay,
                                                 Long resourceId) {
        LocalDateTime fromStart = null;
        LocalDateTime toEnd = null;
        if (singleDay != null) {
            fromStart = singleDay.atStartOfDay();
            toEnd = singleDay.plusDays(1).atStartOfDay();
        } else {
            if (from != null) {
                fromStart = from.atStartOfDay();
            }
            if (to != null) {
                toEnd = to.plusDays(1).atStartOfDay();
            }
        }

        List<Booking> bookings;
        if (role == UserRole.ADMIN) {
            bookings = bookingRepository.findAdminWithFilters(status, resourceId, fromStart, toEnd);
        } else {
            bookings = bookingRepository.findForUserWithFilters(userEmail, status, resourceId, fromStart, toEnd);
        }

        boolean isAdmin = role == UserRole.ADMIN;
        return bookings.stream()
                .map(b -> addLinks(mapToResponse(b, userEmail, role), b, userEmail, isAdmin))
                .toList();
    }

    /**
     * Returns bookings created by the current user.
     */
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getMyBookings(String userEmail) {
        return listBookings(userEmail, UserRole.USER, null, null, null, null, null);
    }

    /**
     * Returns all bookings with optional status/date filters (admin).
     */
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getAllBookings(BookingStatus status, LocalDate date) {
        LocalDateTime startOfDay = null;
        LocalDateTime endOfDay = null;
        if (date != null) {
            startOfDay = date.atStartOfDay();
            endOfDay = date.plusDays(1).atStartOfDay();
        }
        List<Booking> bookings = bookingRepository.findAllWithFilters(status, date, startOfDay, endOfDay);
        return bookings.stream()
                .map(b -> addLinks(mapToResponse(b, null, UserRole.ADMIN), b, null, true))
                .toList();
    }

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

    @Transactional
    public BookingResponseDTO approveBooking(Long id) {
        log.info("Approving booking id: {}", id);
        Booking booking = bookingRepository.findByIdWithJoins(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only PENDING bookings can be approved");
        }

        if (bookingRepository.existsConflictingBooking(
                booking.getResource().getId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getId())) {
            throw new ConflictException("Cannot approve: another booking already occupies this time range");
        }

        booking.setStatus(BookingStatus.APPROVED);
        booking.setRejectionReason(null);
        booking.setQrPayload(buildQrPayload(booking));

        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} approved", id);
        return addLinks(mapToResponse(saved, null, UserRole.ADMIN), saved, null, true);
    }

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
        booking.setQrPayload(null);

        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} rejected with reason: {}", id, reason);
        return addLinks(mapToResponse(saved, null, UserRole.ADMIN), saved, null, true);
    }

    @Transactional
    public BookingResponseDTO cancelBooking(Long id, String userEmail) {
        return cancelBooking(id, userEmail, null);
    }

    @Transactional
    public BookingResponseDTO cancelBooking(Long id, String userEmail, String reason) {
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
        if (reason != null && !reason.isBlank()) {
            booking.setCancellationReason(reason.trim());
        } else {
            booking.setCancellationReason("Not specified");
        }
        booking.setQrPayload(null);

        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} cancelled", id);
        return addLinks(mapToResponse(saved, userEmail, UserRole.USER), saved, userEmail, false);
    }

    @Transactional(readOnly = true)
    public byte[] getBookingQrPng(Long id, String userEmail, UserRole userRole) {
        Booking booking = bookingRepository.findByIdWithJoins(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (userRole == UserRole.USER) {
            if (booking.getUser() == null || booking.getUser().getEmail() == null
                    || !booking.getUser().getEmail().equalsIgnoreCase(userEmail)) {
                throw new ForbiddenException("You do not have access to this booking");
            }
        }

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new BadRequestException("QR code is only available for APPROVED bookings");
        }

        String payload = booking.getQrPayload();
        if (payload == null || payload.isBlank()) {
            payload = buildQrPayload(booking);
        }
        return qrCodeService.encodePng(payload);
    }

    /**
     * Range check (legacy) or full-day availability with optional requested window on {@code date}.
     */
    @Transactional(readOnly = true)
    public AvailabilityResponseDTO checkAvailability(Long resourceId,
                                                     LocalDateTime rangeStart,
                                                     LocalDateTime rangeEnd,
                                                     LocalDate date,
                                                     LocalDateTime windowStart,
                                                     LocalDateTime windowEnd) {
        log.info("Checking availability for resource {} date {} range {}-{}", resourceId, date, rangeStart, rangeEnd);
        resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        AvailabilityResponseDTO dto = new AvailabilityResponseDTO();

        if (date != null) {
            dto.setDate(date);
            List<DaySlotDTO> daySlots = computeDaySlots(resourceId, date);
            dto.setDaySlots(daySlots);
            dto.setAvailableSlots(daySlots.stream()
                    .filter(DaySlotDTO::isAvailable)
                    .map(ds -> new TimeSlotDTO(ds.getStartTime(), ds.getEndTime()))
                    .toList());

            if (windowStart != null && windowEnd != null) {
                if (!windowEnd.isAfter(windowStart)) {
                    dto.setRequestedWindowAvailable(false);
                    dto.setAvailable(false);
                    dto.setMessage("Invalid requested window");
                    return dto;
                }
                if (!windowStart.toLocalDate().equals(date) || !windowEnd.toLocalDate().equals(date)) {
                    throw new BadRequestException("Requested window must fall on the same calendar date as date parameter");
                }
                boolean windowOk = !bookingRepository.existsConflictingBooking(resourceId, windowStart, windowEnd, null);
                dto.setRequestedWindowAvailable(windowOk);
                dto.setAvailable(windowOk);
                dto.setMessage(windowOk ? "Requested window is available" : "Requested window conflicts with an existing booking");
                if (!windowOk) {
                    List<Booking> conflicts = bookingRepository.findConflictingBookings(resourceId, windowStart, windowEnd, null);
                    if (!conflicts.isEmpty()) {
                        Booking first = conflicts.get(0);
                        dto.setConflictingBookingStart(first.getStartTime());
                        dto.setConflictingBookingEnd(first.getEndTime());
                    }
                }
            } else {
                dto.setAvailable(true);
                dto.setMessage("Free slots computed for the selected date");
            }
            return dto;
        }

        if (rangeStart == null || rangeEnd == null || !rangeEnd.isAfter(rangeStart)) {
            dto.setAvailable(false);
            dto.setMessage("Invalid time range");
            return dto;
        }

        List<Booking> conflicts = bookingRepository.findConflictingBookings(resourceId, rangeStart, rangeEnd, null);
        if (conflicts.isEmpty()) {
            dto.setAvailable(true);
            dto.setMessage("Available");
        } else {
            Booking first = conflicts.get(0);
            dto.setAvailable(false);
            dto.setConflictingBookingStart(first.getStartTime());
            dto.setConflictingBookingEnd(first.getEndTime());
            dto.setMessage("Not available due to a conflicting booking");
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public BookingStatsDTO getBookingStats() {
        log.info("Fetching booking stats");
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        BookingStatsDTO dto = new BookingStatsDTO();
        dto.setTotalBookingsToday((int) bookingRepository.countCreatedBetween(startOfDay, endOfDay));
        dto.setTotalBookings(bookingRepository.countAllBookings());
        dto.setPendingCount((int) bookingRepository.countByStatus(BookingStatus.PENDING));
        dto.setApprovedCount((int) bookingRepository.countByStatus(BookingStatus.APPROVED));
        dto.setRejectedCount((int) bookingRepository.countByStatus(BookingStatus.REJECTED));
        dto.setCancelledCount((int) bookingRepository.countByStatus(BookingStatus.CANCELLED));

        List<Object[]> mostBooked = bookingRepository.findMostBookedResource();
        if (mostBooked != null && !mostBooked.isEmpty()) {
            Object[] row = mostBooked.get(0);
            dto.setMostBookedResourceName(Objects.toString(row[0], null));
            dto.setMostBookedResourceCount(((Number) row[1]).intValue());
        } else {
            dto.setMostBookedResourceName(null);
            dto.setMostBookedResourceCount(0);
        }

        List<ResourceBookingCountDTO> topResources = new ArrayList<>();
        for (Object[] r : bookingRepository.findTopBookedResources(PageRequest.of(0, 5))) {
            if (r == null || r.length < 3) continue;
            Long rid = ((Number) r[0]).longValue();
            String name = Objects.toString(r[1], "");
            int cnt = ((Number) r[2]).intValue();
            topResources.add(new ResourceBookingCountDTO(rid, name, cnt));
        }
        dto.setMostBookedResources(topResources);

        List<HourCountDTO> peak = new ArrayList<>();
        for (Object[] r : bookingRepository.findPeakBookingHours(PageRequest.of(0, 5))) {
            if (r == null || r.length < 2) continue;
            int hour = ((Number) r[0]).intValue();
            long cnt = ((Number) r[1]).longValue();
            peak.add(new HourCountDTO(hour, cnt));
        }
        dto.setPeakHours(peak);

        log.info("Fetching booking stats done");
        return dto;
    }

    private List<DaySlotDTO> computeDaySlots(Long resourceId, LocalDate date) {
        List<DaySlotDTO> slots = new ArrayList<>();
        LocalDateTime cursor = LocalDateTime.of(date, AppConstants.RESOURCE_AVAILABLE_FROM);
        final LocalDateTime lastStart = LocalDateTime.of(date, AppConstants.RESOURCE_AVAILABLE_UNTIL).minusHours(1);

        while (!cursor.isAfter(lastStart)) {
            LocalDateTime slotEnd = cursor.plusHours(1);
            boolean free = !bookingRepository.existsConflictingBooking(resourceId, cursor, slotEnd, null);
            slots.add(new DaySlotDTO(cursor, slotEnd, free));
            cursor = slotEnd;
        }
        return slots;
    }

    private String buildQrPayload(Booking booking) {
        return "SMARTCAMPUS|bookingId=%d|resourceId=%d|start=%s|end=%s"
                .formatted(
                        booking.getId(),
                        booking.getResource() != null ? booking.getResource().getId() : 0,
                        booking.getStartTime(),
                        booking.getEndTime()
                );
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
        dto.setCancellationReason(booking.getCancellationReason());
        dto.setQrAvailable(booking.getStatus() == BookingStatus.APPROVED);

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
            dto.add(Link.of(AppConstants.BOOKINGS_BASE + "/" + booking.getId() + "/cancel").withRel("cancel-with-reason"));
        }

        if (booking.getStatus() == BookingStatus.APPROVED && (isOwner || isAdmin)) {
            dto.add(linkTo(methodOn(BookingController.class).getQr(booking.getId(), null)).withRel("qr"));
        }

        if (isAdmin) {
            dto.add(Link.of(AppConstants.BOOKINGS_BASE).withRel("all-bookings"));
        } else {
            dto.add(linkTo(methodOn(BookingController.class).getMyBookings(null)).withRel("my-bookings"));
        }

        return dto;
    }
}
