package com.spribe.booking.dto;

import com.spribe.booking.enums.UnitType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "Unit filter request object")
public class UnitFilterRequest {

    @Schema(description = "Accommodation type", example = "FLAT")
    private UnitType type;

    @Schema(description = "Minimum cost per day", example = "50.00")
    private BigDecimal minCost;

    @Schema(description = "Maximum cost per day", example = "200.00")
    private BigDecimal maxCost;

    @Schema(description = "Exact number of rooms", example = "2")
    private Integer numberOfRooms;

    @Schema(description = "Specific floor", example = "5")
    private Integer floor;

    @Schema(description = "Sort field", example = "costPerDay")
    private String sortBy = "costPerDay";

    @Schema(description = "Sort direction (asc or desc)", example = "asc")
    private String sortDirection = "asc";

    @Schema(description = "Page number (1-based)", example = "1")
    private int page = 1;

    @Schema(description = "Page size", example = "10")
    private int size = 10;

    @Schema(description = "Search start date", example = "2025-08-01")
    private LocalDate startDate;

    @Schema(description = "Search end date", example = "2025-08-10")
    private LocalDate endDate;

}
