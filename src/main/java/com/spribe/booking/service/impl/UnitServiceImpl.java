package com.spribe.booking.service.impl;

import com.spribe.booking.dto.UnitFilterRequest;
import com.spribe.booking.dto.UnitResponse;
import com.spribe.booking.dto.UnitCreateRequest;
import com.spribe.booking.dto.PaginatedResponse;
import com.spribe.booking.entity.Unit;
import com.spribe.booking.enums.BookingStatus;
import com.spribe.booking.repository.BookingRepository;
import com.spribe.booking.repository.UnitRepository;
import com.spribe.booking.service.UnitAvailabilityCacheService;
import com.spribe.booking.service.UnitService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;
    private final BookingRepository bookingRepository;
    private final EntityManager entityManager;
    private final UnitAvailabilityCacheService cacheService;

    @Override
    public Unit addUnit(UnitCreateRequest request) {
        log.info("Adding new unit: {}", request);
        Unit unit = Unit.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .costPerDay(BigDecimal.valueOf(request.getCostPerDay()))
                .numberOfRooms(request.getNumberOfRooms())
                .floor(request.getFloor())
                .build();
        Unit saved = unitRepository.save(unit);
        log.info("Unit added: id={}", saved.getId());
        return saved;
    }

    @Override
    public PaginatedResponse<UnitResponse> searchUnits(UnitFilterRequest filter) {
        log.info("Searching units with filter: {}", filter);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Unit> query = cb.createQuery(Unit.class);
        Root<Unit> root = query.from(Unit.class);

        List<Predicate> predicates = new ArrayList<>();

        if (filter.getType() != null) {
            predicates.add(cb.equal(root.get("type"), filter.getType()));
        }

        if (filter.getMinCost() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("costPerDay"), filter.getMinCost()));
        }

        if (filter.getMaxCost() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("costPerDay"), filter.getMaxCost()));
        }

        if (filter.getNumberOfRooms() != null) {
            predicates.add(cb.equal(root.get("numberOfRooms"), filter.getNumberOfRooms()));
        }

        if (filter.getFloor() != null) {
            predicates.add(cb.equal(root.get("floor"), filter.getFloor()));
        }

        query.where(predicates.toArray(new Predicate[0]));

        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "costPerDay";
        String sortDirection = filter.getSortDirection() != null ? filter.getSortDirection() : "asc";
        Path<?> sortField = root.get(sortBy);
        query.orderBy(sortDirection.equalsIgnoreCase("desc") ? cb.desc(sortField) : cb.asc(sortField));

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Unit> countRoot = countQuery.from(Unit.class);
        countQuery.select(cb.count(countRoot)).where(predicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        int page = filter.getPage() > 0 ? filter.getPage() : 1;
        int size = filter.getSize() > 0 ? filter.getSize() : 10;
        List<Unit> all = entityManager
                .createQuery(query)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();

        List<UnitResponse> results = new ArrayList<>();
        for (Unit unit : all) {
            boolean isAvailable = bookingRepository.findByUnitIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    unit.getId(),
                    Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.PAID, BookingStatus.PENDING),
                    filter.getEndDate(),
                    filter.getStartDate()
            ).isEmpty();
            if (isAvailable) {
                results.add(UnitResponse.from(unit));
            }
        }
        log.info("Unit search completed, found {} available units", results.size());
        return new PaginatedResponse<>(results, total, page, size);
    }

    @Override
    public long getAvailableUnitCount(LocalDate startDate, LocalDate endDate) {
        log.info("Getting available unit count for dates: {} - {}", startDate, endDate);
        Long cached = cacheService.getCachedAvailableCount(startDate, endDate);
        if (cached != null) {
            log.info("Cache hit for available unit count: {}", cached);
            return cached;
        }
        log.info("Cache miss for available unit count, calculating...");
        List<Unit> allUnits = unitRepository.findAll();
        long count = 0;

        for (Unit unit : allUnits) {
            boolean isAvailable = bookingRepository
                    .findByUnitIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            unit.getId(),
                            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.PAID),
                            endDate,
                            startDate
                    ).isEmpty();

            if (isAvailable) count++;
        }

        cacheService.putAvailableCount(startDate, endDate, count);
        log.info("Cached available unit count: {}", count);
        return count;
    }
}
