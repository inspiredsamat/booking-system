package com.spribe.booking.service.impl;

import com.spribe.booking.entity.Unit;
import com.spribe.booking.entity.User;
import com.spribe.booking.enums.UnitType;
import com.spribe.booking.repository.UnitRepository;
import com.spribe.booking.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository userRepository;
    private final UnitRepository unitRepository;

    @PostConstruct
    public void seed() {
        log.info("Starting data seeding");
        if (unitRepository.count() >= 100) {
            log.info("Data seeding skipped, already have 100+ units");
            return;
        }
        try {
            User user = userRepository.findById(1L)
                    .orElseThrow(() -> {
                        log.error("Owner not found for seeding");
                        return new IllegalStateException("Owner not found");
                    });

            Random random = new Random();

            IntStream.range(0, 90).forEach(i -> {
                Unit unit = new Unit();
                unit.setOwner(user);
                unit.setTitle("Unit " + (i + 1));
                unit.setDescription("Generated unit");
                unit.setCostPerDay(BigDecimal.valueOf(50 + random.nextInt(300)));
                unit.setType(UnitType.values()[random.nextInt(UnitType.values().length)]);
                unit.setNumberOfRooms((short) (1 + random.nextInt(5)));
                unit.setFloor((short) (1 + random.nextInt(15)));
                unitRepository.save(unit);
                log.info("Seeded unit: {}", unit.getTitle());
            });
            log.info("Data seeding completed");
        } catch (Exception e) {
            log.error("Error during data seeding", e);
            throw e;
        }
    }
}
