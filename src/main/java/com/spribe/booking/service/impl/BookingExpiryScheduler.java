package com.spribe.booking.service.impl;

import com.spribe.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpiryScheduler {

    private final BookingService bookingService;

    @Scheduled(fixedRate = 60000)
    public void expireUnpaidBookings() {
        log.info("Running scheduled task: expire unpaid bookings");
        bookingService.autoExpireUnpaidBookings();
    }
}
