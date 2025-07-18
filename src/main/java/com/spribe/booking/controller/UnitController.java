package com.spribe.booking.controller;

import com.spribe.booking.dto.UnitFilterRequest;
import com.spribe.booking.dto.UnitResponse;
import com.spribe.booking.dto.UnitCreateRequest;
import com.spribe.booking.dto.PaginatedResponse;
import com.spribe.booking.entity.Unit;
import com.spribe.booking.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/units")
@Tag(name = "Units", description = "Manage accommodation units")
@Slf4j
public class UnitController {

    private final UnitService unitService;

    @Operation(summary = "Create a new unit")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Unit created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<Unit> createUnit(@RequestBody UnitCreateRequest request) {
        log.info("Create unit called with request={}", request);
        Unit created = unitService.addUnit(request);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Search available units with filters")
    @ApiResponse(responseCode = "200", description = "Units found")
    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse<UnitResponse>> searchUnits(@RequestBody UnitFilterRequest request) {
        log.info("Search units called with request={}", request);
        PaginatedResponse<UnitResponse> result = unitService.searchUnits(request);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get count of available units in date range")
    @ApiResponse(responseCode = "200", description = "Available count returned")
    @GetMapping("/available-count")
    public ResponseEntity<Long> getAvailableCount(
            @RequestParam @Parameter(description = "Start date", example = "2025-08-01") LocalDate start,
            @RequestParam @Parameter(description = "End date", example = "2025-08-10") LocalDate end
    ) {
        log.info("Get available count called with start={}, end={}", start, end);
        long count = unitService.getAvailableUnitCount(start, end);
        return ResponseEntity.ok(count);
    }
}
