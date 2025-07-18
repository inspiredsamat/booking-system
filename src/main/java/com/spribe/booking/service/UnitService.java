package com.spribe.booking.service;

import com.spribe.booking.dto.UnitFilterRequest;
import com.spribe.booking.dto.UnitResponse;
import com.spribe.booking.dto.UnitCreateRequest;
import com.spribe.booking.dto.PaginatedResponse;
import com.spribe.booking.entity.Unit;

import java.time.LocalDate;

public interface UnitService {

    Unit addUnit(UnitCreateRequest request);

    PaginatedResponse<UnitResponse> searchUnits(UnitFilterRequest filter);

    long getAvailableUnitCount(LocalDate startDate, LocalDate endDate);
}
