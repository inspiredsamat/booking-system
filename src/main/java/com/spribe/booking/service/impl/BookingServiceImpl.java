package com.spribe.booking.service.impl;

import com.spribe.booking.entity.Booking;
import com.spribe.booking.entity.Payment;
import com.spribe.booking.entity.Unit;
import com.spribe.booking.entity.User;
import com.spribe.booking.enums.BookingStatus;
import com.spribe.booking.enums.PaymentStatus;
import com.spribe.booking.repository.BookingRepository;
import com.spribe.booking.repository.PaymentRepository;
import com.spribe.booking.repository.UnitRepository;
import com.spribe.booking.repository.UserRepository;
import com.spribe.booking.service.BookingService;
import com.spribe.booking.service.UnitAvailabilityCacheService;
import com.spribe.booking.dto.BookingCreateRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final UnitAvailabilityCacheService cacheService;

    @Override
    public Booking bookUnit(BookingCreateRequest request) {
        log.info("Booking unit: userId={}, unitId={}, startDate={}, endDate={}", request.getUserId(), request.getUnitId(), request.getStart(), request.getEnd());
        LocalDate startDate = LocalDate.parse(request.getStart());
        LocalDate endDate = LocalDate.parse(request.getEnd());

        if (!startDate.isBefore(endDate)) {
            log.warn("Start date {} is not before end date {}", startDate, endDate);
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> {
                    log.error("Unit not found: unitId={}", request.getUnitId());
                    return new EntityNotFoundException("Unit not found");
                });

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    log.error("User not found: userId={}", request.getUserId());
                    return new EntityNotFoundException("User not found");
                });

        List<Booking> overlapping = bookingRepository.findByUnitIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                request.getUnitId(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.PAID),
                endDate,
                startDate
        );

        if (!overlapping.isEmpty()) {
            log.warn("Unit {} is not available for the selected dates: {} - {}", request.getUnitId(), startDate, endDate);
            throw new IllegalStateException("Unit is not available for the selected dates.");
        }

        Booking booking = new Booking();
        booking.setUnit(unit);
        booking.setUser(user);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created: bookingId={}", saved.getId());

        Payment payment = new Payment();
        payment.setBooking(saved);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(calculateCost(unit.getCostPerDay(), startDate, endDate));
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);
        log.info("Payment created for bookingId={}", saved.getId());

        cacheService.invalidate(startDate, endDate);
        log.info("Cache invalidated for dates: {} - {}", startDate, endDate);

        return saved;
    }

    @Override
    public void cancelBooking(Long bookingId, Long userId) {
        log.info("Cancelling booking: bookingId={}, userId={}", bookingId, userId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: bookingId={}", bookingId);
                    return new EntityNotFoundException("Booking not found");
                });

        if (!booking.getUser().getId().equals(userId)) {
            log.warn("User {} tried to cancel booking {} not owned by them", userId, bookingId);
            throw new SecurityException("You can only cancel your own bookings.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED) {
            log.info("Booking already cancelled or expired: bookingId={}", bookingId);
            return;
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking cancelled: bookingId={}", bookingId);

        cacheService.invalidate(booking.getStartDate(), booking.getEndDate());
        log.info("Cache invalidated for dates: {} - {}", booking.getStartDate(), booking.getEndDate());
    }

    @Override
    public void payForBooking(Long bookingId, Long userId) {
        log.info("Processing payment: bookingId={}, userId={}", bookingId, userId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: bookingId={}", bookingId);
                    return new EntityNotFoundException("Booking not found");
                });

        if (!booking.getUser().getId().equals(userId)) {
            log.warn("User {} tried to pay for booking {} not owned by them", userId, bookingId);
            throw new SecurityException("You can only pay for your own bookings.");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("Booking {} is not in PENDING state for payment", bookingId);
            throw new IllegalStateException("Booking must be in PENDING state.");
        }

        booking.setStatus(BookingStatus.PAID);

        Payment payment = paymentRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Payment not found for bookingId={}", bookingId);
                    return new EntityNotFoundException("Payment not found");
                });
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentTime(LocalDateTime.now());

        bookingRepository.save(booking);
        paymentRepository.save(payment);
        log.info("Payment processed and booking marked as PAID: bookingId={}", bookingId);
    }

    @Override
    public void autoExpireUnpaidBookings() {
        log.info("Auto-expiring unpaid bookings");
        List<Booking> bookings = bookingRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Booking b : bookings) {
            if (b.getStatus() == BookingStatus.PENDING &&
                    b.getCreatedAt().plusMinutes(15).isBefore(now)) {

                b.setStatus(BookingStatus.EXPIRED);
                bookingRepository.save(b);
                log.info("Booking expired: bookingId={}", b.getId());

                Optional<Payment> p = paymentRepository.findById(b.getId());
                p.ifPresent(payment -> {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setPaymentTime(LocalDateTime.now());
                    paymentRepository.save(payment);
                    log.info("Payment marked as FAILED for expired bookingId={}", b.getId());
                });

                cacheService.invalidate(b.getStartDate(), b.getEndDate());
                log.info("Cache invalidated for expired booking: {} - {}", b.getStartDate(), b.getEndDate());
            }
        }
    }

    private BigDecimal calculateCost(BigDecimal costPerDay, LocalDate start, LocalDate end) {
        long days = start.until(end).getDays();
        BigDecimal base = costPerDay.multiply(BigDecimal.valueOf(days));
        return base.add(base.multiply(BigDecimal.valueOf(0.15)));
    }
}