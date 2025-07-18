package com.spribe.booking.service.impl;

import com.spribe.booking.dto.UnitCreateRequest;
import com.spribe.booking.entity.Booking;
import com.spribe.booking.entity.Unit;
import com.spribe.booking.enums.UnitType;
import com.spribe.booking.repository.BookingRepository;
import com.spribe.booking.repository.UnitRepository;
import com.spribe.booking.service.UnitAvailabilityCacheService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnitServiceImplTest {

    private UnitServiceImpl service;
    private UnitRepository unitRepository;
    private BookingRepository bookingRepository;
    private EntityManager entityManager;
    private UnitAvailabilityCacheService cacheService;

    @BeforeEach
    void setUp() {
        unitRepository = mock(UnitRepository.class);
        bookingRepository = mock(BookingRepository.class);
        entityManager = mock(EntityManager.class);
        cacheService = mock(UnitAvailabilityCacheService.class);
        service = new UnitServiceImpl(unitRepository, bookingRepository, entityManager, cacheService);
    }

    @Test
    void getAvailableUnitCount_shouldReturnCorrectCount() {
        Unit u1 = new Unit(); u1.setId(1L);
        Unit u2 = new Unit(); u2.setId(2L);

        when(unitRepository.findAll()).thenReturn(List.of(u1, u2));

        // Unit 1 is booked, unit 2 is free
        when(bookingRepository.findByUnitIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(1L), any(), any(), any()))
                .thenReturn(List.of(new Booking()));

        when(bookingRepository.findByUnitIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(2L), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        when(cacheService.getCachedAvailableCount(any(), any())).thenReturn(null);

        long count = service.getAvailableUnitCount(LocalDate.now(), LocalDate.now().plusDays(3));

        assertEquals(1, count);
    }

    @Test
    void addUnit_shouldSaveAndReturn() {
        UnitCreateRequest req = new UnitCreateRequest();
        req.setTitle("Test Unit");
        req.setDescription("A nice place");
        req.setType(UnitType.APARTMENTS);
        req.setNumberOfRooms((short)2);
        req.setCostPerDay(100.0);
        req.setFloor((short)3);

        Unit expected = Unit.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .type(req.getType())
                .numberOfRooms(req.getNumberOfRooms())
                .costPerDay(java.math.BigDecimal.valueOf(req.getCostPerDay()))
                .floor(req.getFloor())
                .build();

        when(unitRepository.save(any(Unit.class))).thenReturn(expected);
        Unit result = service.addUnit(req);
        assertEquals(expected.getTitle(), result.getTitle());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getType(), result.getType());
        assertEquals(expected.getNumberOfRooms(), result.getNumberOfRooms());
        assertEquals(expected.getCostPerDay(), result.getCostPerDay());
        assertEquals(expected.getFloor(), result.getFloor());
    }

}