package com.spribe.booking.service;

import java.time.LocalDate;

public interface UnitAvailabilityCacheService {


    Long getCachedAvailableCount(LocalDate startDate, LocalDate endDate);

    void putAvailableCount(LocalDate startDate, LocalDate endDate, long count);

    void invalidate(LocalDate startDate, LocalDate endDate);
}
