package com.smart_campus_system.demo.controller;

import com.smart_campus_system.demo.config.AppConstants;
import com.smart_campus_system.demo.dto.*;
import com.smart_campus_system.demo.model.BookingStatus;
import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(AppConstants.BOOKINGS_BASE)
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    private static final String DEV_FALLBACK_USER_EMAIL = "demo.user@smartcampus.local";

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN','TECHNICIAN')")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> createBooking(@Valid @RequestBody BookingRequestDTO dto,
                                                                         Authentication authentication) {
        String email = authentication != null ? authentication.getName() : DEV_FALLBACK_USER_EMAIL;
        BookingResponseDTO created = bookingService.createBooking(dto, email);
        return ResponseEntity.status(201).body(ApiResponse.ok(created, "Booking created and pending approval"));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getMyBookings(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : DEV_FALLBACK_USER_EMAIL;
        List<BookingResponseDTO> bookings = bookingService.getMyBookings(email);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.ok(bookings, "My bookings fetched successfully"));
    }

    /**
     * Unified listing: users see their bookings; admins see all. Supports status, date range, and resource filters.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long resourceId,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : DEV_FALLBACK_USER_EMAIL;
        Role role = authentication != null
                && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                ? Role.ADMIN
                : (authentication == null ? Role.ADMIN : Role.USER);
        List<BookingResponseDTO> bookings = bookingService.listBookings(email, role, status, from, to, date, resourceId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.ok(bookings, "Bookings fetched successfully"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingStatsDTO>> getStats() {
        BookingStatsDTO stats = bookingService.getBookingStats();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate())
                .body(ApiResponse.ok(stats, "Booking stats fetched successfully"));
    }

    /**
     * Without {@code date}: checks a concrete {@code startTime}/{@code endTime} range (ISO-8601).<br>
     * With {@code date}: returns free 1-hour slots for that day; optional {@code startTime}/{@code endTime} on the same
     * calendar day checks whether that window is bookable.
     */
    @GetMapping("/availability")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<AvailabilityResponseDTO>> checkAvailability(
            @RequestParam Long resourceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        AvailabilityResponseDTO availability;
        if (date != null) {
            availability = bookingService.checkAvailability(resourceId, null, null, date, startTime, endTime);
        } else {
            availability = bookingService.checkAvailability(resourceId, startTime, endTime, null, null, null);
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS).cachePrivate())
                .body(ApiResponse.ok(availability, "Availability checked"));
    }

    @GetMapping("/{id}/qr")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<byte[]> getQr(@PathVariable Long id, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : DEV_FALLBACK_USER_EMAIL;
        Role role = authentication != null
                && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                ? Role.ADMIN
                : (authentication == null ? Role.ADMIN : Role.USER);
        byte[] png = bookingService.getBookingQrPng(id, email, role);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noStore())
                .body(png);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> getBookingById(@PathVariable Long id,
                                                                          Authentication authentication) {
        String email = authentication != null ? authentication.getName() : DEV_FALLBACK_USER_EMAIL;
        Role role = authentication != null
                && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                ? Role.ADMIN
                : (authentication == null ? Role.ADMIN : Role.USER);
        BookingResponseDTO booking = bookingService.getBookingById(id, email, role);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(ApiResponse.ok(booking, "Booking fetched successfully"));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> approve(@PathVariable Long id) {
        BookingResponseDTO updated = bookingService.approveBooking(id);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Booking approved"));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> reject(@PathVariable Long id,
                                                                  @Valid @RequestBody RejectRequestDTO dto) {
        BookingResponseDTO updated = bookingService.rejectBooking(id, dto.getReason());
        return ResponseEntity.ok(ApiResponse.ok(updated, "Booking rejected"));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> cancel(@PathVariable Long id,
                                                                  Authentication authentication) {
        String email = authentication != null ? authentication.getName() : DEV_FALLBACK_USER_EMAIL;
        BookingResponseDTO updated = bookingService.cancelBooking(id, email);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Booking cancelled"));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> cancelWithReason(@PathVariable Long id,
                                                                              @Valid @RequestBody CancelBookingRequestDTO dto,
                                                                              Authentication authentication) {
        String email = authentication != null ? authentication.getName() : DEV_FALLBACK_USER_EMAIL;
        BookingResponseDTO updated = bookingService.cancelBooking(id, email, dto.getReason());
        return ResponseEntity.ok(ApiResponse.ok(updated, "Booking cancelled"));
    }
}
