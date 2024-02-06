package com.modoospace.reservation.repository;

import static com.modoospace.reservation.domain.QReservation.reservation;

import com.modoospace.reservation.domain.DateTimeRange;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.space.domain.Facility;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ReservationQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

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
            .where(facilityEq(facility),
                statusNe(ReservationStatus.CANCELED),
                startDateTimeBefore(dateTimeRange.getEndDateTime()),
                endDateTimeAfter(dateTimeRange.getStartDateTime()))
            .fetch();
    }

    private BooleanExpression facilityEq(Facility facility) {
        return facility != null ? reservation.facility.eq(facility) : null;
    }

    private BooleanExpression statusNe(ReservationStatus status) {
        return status != null ? reservation.status.ne(status) : null;
    }

    private BooleanExpression startDateTimeBefore(LocalDateTime dateTime) {
        return dateTime != null ? reservation.dateTimeRange.startDateTime.before(dateTime) : null;
    }

    private BooleanExpression endDateTimeAfter(LocalDateTime dateTime) {
        return dateTime != null ? reservation.dateTimeRange.endDateTime.after(dateTime) : null;
    }
}
