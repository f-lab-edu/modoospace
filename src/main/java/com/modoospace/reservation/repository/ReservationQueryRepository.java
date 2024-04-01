package com.modoospace.reservation.repository;

import static com.modoospace.reservation.domain.QReservation.reservation;
import static com.modoospace.space.domain.QFacility.facility;
import static com.modoospace.space.domain.QSpace.space;

import com.modoospace.member.domain.QMember;
import com.modoospace.reservation.controller.dto.search.CommonSearchRequest;
import com.modoospace.reservation.domain.DateTimeRange;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.space.domain.Facility;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ReservationQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private static final QMember visitor = new QMember("visitor");

    public List<Reservation> findActiveReservations(Facility facility, LocalDate date) {
        return findConflictingReservation(facility, new DateTimeRange(date, 0, date, 24));
    }

    public Boolean isConflictingReservation(Facility facility, DateTimeRange dateTimeRange) {
        return !findConflictingReservation(facility, dateTimeRange).isEmpty();
    }

    private List<Reservation> findConflictingReservation(Facility facility,
            DateTimeRange dateTimeRange) {

        return jpaQueryFactory
                .selectFrom(reservation)
                .where(reservation.facility.eq(facility),
                        reservation.status.ne(ReservationStatus.CANCELED),
                        reservation.dateTimeRange.startDateTime.before(
                                dateTimeRange.getEndDateTime()),
                        reservation.dateTimeRange.endDateTime.after(
                                dateTimeRange.getStartDateTime())
                )
                .fetch();
    }

    public Page<Reservation> searchSpace(CommonSearchRequest request, Pageable pageable) {

        List<Reservation> content = jpaQueryFactory
                .selectFrom(reservation)
                .innerJoin(reservation.facility, facility).fetchJoin()
                .innerJoin(reservation.visitor, visitor).fetchJoin()
                .innerJoin(facility.space, space)
                .where(
                        eqVisitorId(request)
                        , eqHostId(request)
                        , eqSpaceId(request)
                        , containsSpaceName(request)
                        , eqStatus(request)
                )
                .orderBy(reservation.createdTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Reservation> countQuery = jpaQueryFactory
                .selectFrom(reservation)
                .innerJoin(reservation.facility, facility)
                .innerJoin(reservation.visitor, visitor)
                .innerJoin(facility.space, space)
                .where(
                        eqVisitorId(request)
                        , eqHostId(request)
                        , eqSpaceId(request)
                        , containsSpaceName(request)
                        , eqStatus(request)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    private static BooleanExpression eqVisitorId(CommonSearchRequest request) {
        return request.getVisitorId() != null ? visitor.id.eq(request.getVisitorId())
                : null;
    }

    private static BooleanExpression eqHostId(CommonSearchRequest request) {
        return request.getHostId() != null ? space.host.id.eq(request.getHostId()) : null;
    }

    private static BooleanExpression eqSpaceId(CommonSearchRequest request) {
        return request.getSpaceId() != null ? space.id.eq(request.getSpaceId()) : null;
    }

    private static BooleanExpression containsSpaceName(CommonSearchRequest request) {
        return request.getSpaceName() != null ? space.name.contains(request.getSpaceName()) : null;
    }

    private static BooleanExpression eqStatus(CommonSearchRequest request) {
        return request.getStatus() != null ? reservation.status.eq(request.getStatus()) : null;
    }
}
