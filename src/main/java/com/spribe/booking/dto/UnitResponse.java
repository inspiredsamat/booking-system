package com.spribe.booking.dto;

import com.spribe.booking.entity.Unit;
import com.spribe.booking.enums.UnitType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UnitResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal costPerDay;
    private UnitType type;
    private int numberOfRooms;
    private int floor;

    public static UnitResponse from(Unit unit) {
        UnitResponse dto = new UnitResponse();
        dto.id = unit.getId();
        dto.title = unit.getTitle();
        dto.description = unit.getDescription();
        dto.costPerDay = unit.getCostPerDay();
        dto.type = unit.getType();
        dto.numberOfRooms = unit.getNumberOfRooms();
        dto.floor = unit.getFloor();
        return dto;
    }
}
