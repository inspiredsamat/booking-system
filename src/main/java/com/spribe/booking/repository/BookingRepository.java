package com.spribe.booking.repository;

import com.spribe.booking.entity.Booking;
import com.spribe.booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUnitIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long unitId,
            List<BookingStatus> statuses,
            LocalDate endDate,
            LocalDate startDate
    );
}
