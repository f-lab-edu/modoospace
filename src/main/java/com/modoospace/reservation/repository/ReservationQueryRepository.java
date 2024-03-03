package com.modoospace.reservation.repository;

import com.modoospace.reservation.domain.DateTimeRange;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.space.domain.Facility;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.modoospace.reservation.domain.QReservation.reservation;

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

    private List<Reservation> findConflictingReservation(Facility facility, DateTimeRange dateTimeRange) {

        return jpaQueryFactory
                .selectFrom(reservation)
                .where(reservation.facility.eq(facility),
                        reservation.status.ne(ReservationStatus.CANCELED),
                        reservation.dateTimeRange.startDateTime.before(dateTimeRange.getEndDateTime()),
                        reservation.dateTimeRange.endDateTime.after(dateTimeRange.getStartDateTime())
                )
                .fetch();
    }
}
