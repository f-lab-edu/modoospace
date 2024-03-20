package com.modoospace.space.repository;

import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.space.controller.dto.space.SpaceSearchRequest;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.TimeRange;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.modoospace.member.domain.QMember.member;
import static com.modoospace.reservation.domain.QReservation.reservation;
import static com.modoospace.space.domain.QCategory.category;
import static com.modoospace.space.domain.QFacility.facility;
import static com.modoospace.space.domain.QSchedule.schedule;
import static com.modoospace.space.domain.QSpace.space;

@RequiredArgsConstructor
@Repository
public class SpaceQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private final SpaceIndexQueryRepository spaceIndexQueryRepository;

    public Page<Space> searchSpace(SpaceSearchRequest searchRequest, Pageable pageable) {

        BooleanExpression spaceIdInExpression = spaceIdInQueryResult(searchRequest.getQuery());
        List<Space> content = jpaQueryFactory
                .selectFrom(space)
                .join(space.host, member).fetchJoin()
                .join(space.category, category).fetchJoin()
                .where(
                        spaceIdInExpression
                        , eqDepthFirst(searchRequest.getDepthFirst())
                        , eqDepthSecond(searchRequest.getDepthSecond())
                        , eqDepthThird(searchRequest.getDepthThird())
                        , facilitySubQuery(searchRequest.getMaxUser()
                                , searchRequest.getUseDate()
                                , searchRequest.getTimeRange())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Space> countQuery = jpaQueryFactory
                .selectFrom(space)
                .where(
                        spaceIdInExpression
                        , eqDepthFirst(searchRequest.getDepthFirst())
                        , eqDepthSecond(searchRequest.getDepthSecond())
                        , eqDepthThird(searchRequest.getDepthThird())
                        , facilitySubQuery(searchRequest.getMaxUser()
                                , searchRequest.getUseDate()
                                , searchRequest.getTimeRange())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    public Page<Space> searchSpaceQuery(SpaceSearchRequest searchRequest, Pageable pageable) {
        List<Space> content = jpaQueryFactory
                .selectFrom(space)
                .join(space.host, member).fetchJoin()
                .join(space.category, category).fetchJoin()
                .where(
                        findSpaceByQuery(searchRequest.getQuery())
                        , eqDepthFirst(searchRequest.getDepthFirst())
                        , eqDepthSecond(searchRequest.getDepthSecond())
                        , eqDepthThird(searchRequest.getDepthThird())
                        , facilitySubQuery(searchRequest.getMaxUser()
                                , searchRequest.getUseDate()
                                , searchRequest.getTimeRange())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Space> countQuery = jpaQueryFactory
                .selectFrom(space)
                .where(
                        findSpaceByQuery(searchRequest.getQuery())
                        , eqDepthFirst(searchRequest.getDepthFirst())
                        , eqDepthSecond(searchRequest.getDepthSecond())
                        , eqDepthThird(searchRequest.getDepthThird())
                        , facilitySubQuery(searchRequest.getMaxUser()
                                , searchRequest.getUseDate()
                                , searchRequest.getTimeRange())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    /**
     * 엘라스틱 서치를 사용한 검색
     */
    private BooleanExpression spaceIdInQueryResult(String query) {
        return query != null ? space.id.in(spaceIndexQueryRepository.findIdByQuery(query)) : null;
    }

    /**
     * 쿼리를 사용한 검색
     */
    private BooleanBuilder findSpaceByQuery(String query) {
        return query != null ? likeQuery(query) : null;
    }

    private BooleanBuilder likeQuery(String query) {
        String[] terms = query.split("\\s+");

        BooleanBuilder builder = new BooleanBuilder();
        for (String term : terms) {
            String searchPattern = "%" + term + "%";
            builder.and(space.name.like(searchPattern)
                    .or(space.description.like(searchPattern))
                    .or(space.category.name.like(searchPattern))
                    .or(space.address.depthFirst.like(searchPattern))
                    .or(space.address.depthSecond.like(searchPattern))
                    .or(space.address.depthThird.like(searchPattern)));
        }
        return builder;
    }

    private BooleanExpression eqDepthFirst(String depthFirst) {
        return depthFirst != null ? space.address.depthFirst.eq(depthFirst) : null;
    }

    private BooleanExpression eqDepthSecond(String depthSecond) {
        return depthSecond != null ? space.address.depthSecond.eq(depthSecond) : null;
    }

    private BooleanExpression eqDepthThird(String depthThird) {
        return depthThird != null ? space.address.depthThird.eq(depthThird) : null;
    }

    private BooleanExpression facilitySubQuery(Integer maxUser, LocalDate useDate,
                                               TimeRange timeRange) {
        return jpaQueryFactory
                .selectFrom(facility)
                .where(
                        facility.space.eq(space)
                        , facility.reservationEnable.eq(true)
                        , biggerThanMaxUser(maxUser)
                        , scheduleSubQuery(useDate, timeRange)
                        , reservationSubQuery(useDate, timeRange)
                )
                .exists();
    }

    private BooleanExpression biggerThanMaxUser(Integer maxUser) {
        return maxUser != null ? facility.maxUser.goe(maxUser) : null;
    }

    /**
     * 가능한 스케줄을 포함하고있는지 확인한다.
     */
    private BooleanExpression scheduleSubQuery(LocalDate useDate, TimeRange timeRange) {
        if (useDate == null) {
            return null;
        }

        return jpaQueryFactory
                .selectFrom(schedule)
                .where(
                        schedule.facility.eq(facility)
                        , schedule.date.eq(useDate)
                        , includingTimeRange(timeRange)
                )
                .exists();
    }

    private BooleanExpression includingTimeRange(TimeRange timeRange) {
        if (timeRange == null) {
            return null;
        }

        return (schedule.timeRange.startTime.loe(timeRange.getStartTime()))
                .and((schedule.timeRange.endTime.goe(timeRange.getEndTime())));
    }

    /**
     * 겹치는 예약이 있는지 확인한다.
     */
    private BooleanExpression reservationSubQuery(LocalDate useDate, TimeRange timeRange) {
        if (useDate == null || timeRange == null) {
            return null;
        }

        LocalDateTime startDateTime = LocalDateTime.of(useDate, timeRange.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(useDate, timeRange.getEndTime());
        return jpaQueryFactory
                .selectFrom(reservation)
                .where(reservation.facility.eq(facility)
                        , reservation.status.ne(ReservationStatus.CANCELED)
                        , reservation.dateTimeRange.startDateTime.before(endDateTime)
                        , reservation.dateTimeRange.endDateTime.after(startDateTime)
                )
                .notExists();
    }
}
