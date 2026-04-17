package com.smart_campus_system.demo.controller;

import com.smart_campus_system.demo.config.AppConstants;
import com.smart_campus_system.demo.dto.*;
import com.smart_campus_system.demo.model.BookingStatus;
import com.smart_campus_system.demo.model.UserRole;
import com.smart_campus_system.demo.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
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
    @PreAuthorize("hasRole('USER')")
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

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> getBookingById(@PathVariable Long id,
                                                                          Authentication authentication) {
        String email = authentication != null ? authentication.getName() : DEV_FALLBACK_USER_EMAIL;
        UserRole role = authentication != null
                && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                ? UserRole.ADMIN
                : (authentication == null ? UserRole.ADMIN : UserRole.USER);
        BookingResponseDTO booking = bookingService.getBookingById(id, email, role);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(ApiResponse.ok(booking, "Booking fetched successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getAllBookings(@RequestParam(required = false) BookingStatus status,
                                                                                @RequestParam(required = false) LocalDate date) {
        List<BookingResponseDTO> bookings = bookingService.getAllBookings(status, date);
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

    @GetMapping("/availability")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AvailabilityResponseDTO>> checkAvailability(@RequestParam Long resourceId,
                                                                                  @RequestParam LocalDateTime startTime,
                                                                                  @RequestParam LocalDateTime endTime) {
        AvailabilityResponseDTO availability = bookingService.checkAvailability(resourceId, startTime, endTime);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS).cachePrivate())
                .body(ApiResponse.ok(availability, "Availability checked"));
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
}
