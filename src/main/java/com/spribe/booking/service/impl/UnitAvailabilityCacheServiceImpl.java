package com.spribe.booking.service.impl;

import com.spribe.booking.service.UnitAvailabilityCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnitAvailabilityCacheServiceImpl implements UnitAvailabilityCacheService {

    private static final Duration TTL = Duration.ofMinutes(10);
    private final RedisTemplate<String, Long> redisTemplate;

    private String key(LocalDate start, LocalDate end) {
        return "unit_availability:" + start + ":" + end;
    }

    @Override
    public Long getCachedAvailableCount(LocalDate startDate, LocalDate endDate) {
        try {
            Long value = redisTemplate.opsForValue().get(key(startDate, endDate));
            log.info("Cache get: key={}, value={}", key(startDate, endDate), value);
            return value;
        } catch (Exception e) {
            log.error("Error getting cache for key={}", key(startDate, endDate), e);
            return null;
        }
    }

    @Override
    public void putAvailableCount(LocalDate startDate, LocalDate endDate, long count) {
        try {
            redisTemplate.opsForValue()
                    .set(key(startDate, endDate), count, TTL);
        } catch (Exception ignored) {}
    }

    @Override
    public void invalidate(LocalDate startDate, LocalDate endDate) {
        try {
            redisTemplate.delete(key(startDate, endDate));
            log.info("Cache invalidated: key={}", key(startDate, endDate));
        } catch (Exception e) {
            log.error("Error invalidating cache for key={}", key(startDate, endDate), e);
        }
    }
}
