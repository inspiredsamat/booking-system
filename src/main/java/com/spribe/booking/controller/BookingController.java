package com.spribe.booking.controller;

import com.spribe.booking.entity.Booking;
import com.spribe.booking.service.BookingService;
import com.spribe.booking.dto.BookingCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Book, cancel, or pay for units")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Book a unit")
    @ApiResponse(responseCode = "201", description = "Booking created")
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingCreateRequest request) {
        log.info("Create booking called with request={}", request);
        Booking booking = bookingService.bookUnit(request);
        return ResponseEntity.status(201).body(booking);
    }

    @Operation(summary = "Pay for a booking")
    @ApiResponse(responseCode = "204", description = "Booking marked as paid")
    @PostMapping("/{bookingId}/pay")
    public ResponseEntity<Void> payBooking(
            @PathVariable Long bookingId,
            @RequestParam Long userId
    ) {
        log.info("Pay booking called with bookingId={}, userId={}", bookingId, userId);
        bookingService.payForBooking(bookingId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancel a booking")
    @ApiResponse(responseCode = "204", description = "Booking cancelled")
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId,
            @RequestParam Long userId
    ) {
        log.info("Cancel booking called with bookingId={}, userId={}", bookingId, userId);
        bookingService.cancelBooking(bookingId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Trigger auto-expiry of unpaid bookings")
    @ApiResponse(responseCode = "204", description = "Expired bookings handled")
    @PostMapping("/auto-expire")
    public ResponseEntity<Void> autoExpireBookings() {
        log.info("Auto expire bookings called");
        bookingService.autoExpireUnpaidBookings();
        return ResponseEntity.noContent().build();
    }
}
