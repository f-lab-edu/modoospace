package com.modoospace.space.repository;

import static com.modoospace.space.domain.QFacility.facility;

import com.modoospace.space.controller.dto.facility.FacilityReadDto;
import com.modoospace.space.controller.dto.facility.FacilitySearchDto;
import com.modoospace.space.domain.Facility;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class FacilityQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public Page<FacilityReadDto> searchFacility(Long spaceId, FacilitySearchDto searchDto,
        Pageable pageable) {

        List<FacilityReadDto> content = jpaQueryFactory
            .select(
                Projections.constructor(FacilityReadDto.class
                    , facility.id
                    , facility.name
                    , facility.reservationEnable
                    , facility.minUser
                    , facility.maxUser
                    , facility.description)
            )
            .from(facility)
            .where(spaceIdEq(spaceId)
                , nameContains(searchDto.getName())
                , reservationEnableEq(searchDto.getReservationEnable()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Facility> countQuery = jpaQueryFactory
            .selectFrom(facility)
            .where(spaceIdEq(spaceId)
                , nameContains(searchDto.getName())
                , reservationEnableEq(searchDto.getReservationEnable()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    private BooleanExpression spaceIdEq(Long spaceId) {
        return spaceId != null ? facility.space.id.eq(spaceId) : null;
    }

    private BooleanExpression nameContains(String name) {
        return name != null ? facility.name.contains(name) : null;
    }

    private BooleanExpression reservationEnableEq(Boolean reservationEnable) {
        return reservationEnable != null ? facility.reservationEnable.eq(reservationEnable) : null;
    }
}
