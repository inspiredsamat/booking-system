package com.spribe.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BookingCreateRequest {
    @Schema(description = "User ID")
    private Long userId;
    @Schema(description = "Unit ID")
    private Long unitId;
    @Schema(description = "Start date", example = "2025-08-01")
    private String start;
    @Schema(description = "End date", example = "2025-08-05")
    private String end;
} 