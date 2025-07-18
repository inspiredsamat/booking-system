package com.spribe.booking.service.impl;

import com.spribe.booking.entity.Booking;
import com.spribe.booking.entity.Payment;
import com.spribe.booking.entity.Unit;
import com.spribe.booking.entity.User;
import com.spribe.booking.enums.BookingStatus;
import com.spribe.booking.enums.PaymentStatus;
import com.spribe.booking.repository.BookingRepository;
import com.spribe.booking.dto.BookingCreateRequest;
import com.spribe.booking.repository.PaymentRepository;
import com.spribe.booking.repository.UnitRepository;
import com.spribe.booking.repository.UserRepository;
import com.spribe.booking.service.BookingService;
import com.spribe.booking.service.UnitAvailabilityCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookingServiceImplTest {

    private BookingService service;
    private BookingRepository bookingRepo;
    private UnitRepository unitRepo;
    private UserRepository userRepo;
    private PaymentRepository paymentRepo;
    private UnitAvailabilityCacheService cacheService;

    private Unit unit;
    private User user;

    @BeforeEach
    void setup() {
        bookingRepo = mock(BookingRepository.class);
        unitRepo = mock(UnitRepository.class);
        userRepo = mock(UserRepository.class);
        paymentRepo = mock(PaymentRepository.class);
        cacheService = mock(UnitAvailabilityCacheService.class);
        service = new BookingServiceImpl(bookingRepo, unitRepo, userRepo, paymentRepo, cacheService);

        unit = new Unit();
        unit.setId(1L);
        unit.setCostPerDay(BigDecimal.valueOf(100));

        user = new User();
        user.setId(2L);
    }

    @Test
    void bookUnit_shouldCreateBooking() {
        when(unitRepo.findById(1L)).thenReturn(Optional.of(unit));
        when(userRepo.findById(2L)).thenReturn(Optional.of(user));
        when(bookingRepo.findByUnitIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(Collections.emptyList());

        when(bookingRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(paymentRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        BookingCreateRequest req = new BookingCreateRequest();
        req.setUserId(2L);
        req.setUnitId(1L);
        req.setStart("2025-08-01");
        req.setEnd("2025-08-03");
        Booking result = service.bookUnit(req);

        assertEquals(BookingStatus.PENDING, result.getStatus());
        assertEquals(user, result.getUser());
        assertEquals(unit, result.getUnit());
    }

    @Test
    void bookUnit_shouldFailOnOverlap() {
        when(unitRepo.findById(1L)).thenReturn(Optional.of(unit));
        when(userRepo.findById(2L)).thenReturn(Optional.of(user));
        when(bookingRepo.findByUnitIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(List.of(new Booking()));

        BookingCreateRequest req = new BookingCreateRequest();
        req.setUserId(2L);
        req.setUnitId(1L);
        req.setStart("2025-08-01");
        req.setEnd("2025-08-03");
        assertThrows(IllegalStateException.class,
                () -> service.bookUnit(req));
    }

    @Test
    void cancelBooking_shouldWork() {
        Booking b = new Booking();
        b.setId(1L);
        b.setUser(user);
        b.setStatus(BookingStatus.PENDING);
        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        service.cancelBooking(1L, 2L);
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
    }

    @Test
    void cancelBooking_shouldFailIfUserMismatch() {
        Booking b = new Booking();
        b.setId(1L);
        b.setUser(new User(1L, "Full Name", "email@mail.com", LocalDateTime.now()));
        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        assertThrows(SecurityException.class, () -> service.cancelBooking(1L, 99L));
    }

    @Test
    void payForBooking_shouldWork() {
        Booking b = new Booking();
        b.setId(1L);
        b.setUser(user);
        b.setStatus(BookingStatus.PENDING);
        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));

        Payment p = new Payment();
        when(paymentRepo.findById(1L)).thenReturn(Optional.of(p));

        service.payForBooking(1L, 2L);

        assertEquals(BookingStatus.PAID, b.getStatus());
        assertEquals(PaymentStatus.PAID, p.getStatus());
        assertNotNull(p.getPaymentTime());
    }

    @Test
    void payForBooking_shouldFailIfExpired() {
        Booking b = new Booking();
        b.setId(1L);
        b.setUser(user);
        b.setStatus(BookingStatus.CANCELLED);
        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        assertThrows(IllegalStateException.class, () -> service.payForBooking(1L, 2L));
    }

    @Test
    void autoExpireUnpaidBookings_shouldExpireOldOnes() {
        Booking b1 = new Booking();
        b1.setId(1L);
        b1.setStatus(BookingStatus.PENDING);
        b1.setCreatedAt(LocalDateTime.now().minusMinutes(20));
        b1.setUser(user);

        Booking b2 = new Booking();
        b2.setId(2L);
        b2.setStatus(BookingStatus.CONFIRMED); // not affected
        b2.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        b2.setUser(user);

        when(bookingRepo.findAll()).thenReturn(List.of(b1, b2));

        Payment p = new Payment();
        when(paymentRepo.findById(1L)).thenReturn(Optional.of(p));

        service.autoExpireUnpaidBookings();

        assertEquals(BookingStatus.EXPIRED, b1.getStatus());
        assertEquals(PaymentStatus.FAILED, p.getStatus());
    }

}