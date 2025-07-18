package com.spribe.booking.service;

import com.spribe.booking.entity.Booking;
import com.spribe.booking.dto.BookingCreateRequest;

public interface BookingService {

    Booking bookUnit(BookingCreateRequest request);

    void cancelBooking(Long bookingId, Long userId);

    void payForBooking(Long bookingId, Long userId);

    void autoExpireUnpaidBookings();
}
