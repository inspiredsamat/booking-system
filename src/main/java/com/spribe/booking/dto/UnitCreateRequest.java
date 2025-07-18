package com.spribe.booking.dto;

import com.spribe.booking.enums.UnitType;
import lombok.Data;

@Data
public class UnitCreateRequest {
    private String title;
    private String description;
    private UnitType type;
    private short numberOfRooms;
    private double costPerDay;
    private short floor;
}