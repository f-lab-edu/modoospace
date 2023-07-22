package com.modoospace.reservation.repository;

import static com.modoospace.reservation.domain.QReservation.reservation;

import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.space.domain.Facility;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ReservationQueryRepository {

  private final JPAQueryFactory jpaQueryFactory;

  /**
   * 동일한 시설의 예약 중 겹치는 예약이 있는지 확인합니다.
   *
   * @param facility 시설
   * @param start    시작 시간
   * @param end      종료 시간
   * @return 겹치는 예약이 있는 경우 true, 없는 경우 false
   */
  public Boolean isOverlappingReservation(Facility facility, LocalDateTime start,
      LocalDateTime end) {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(reservation.facility.eq(facility))
        .and(reservation.status.in(ReservationStatus.getActiveStatuses()))
        .and(reservation.reservationStart.before(end))
        .and(reservation.reservationEnd.after(start));

    JPAQuery<Reservation> query = jpaQueryFactory
        .select(reservation)
        .from(reservation)
        .where(builder);

    return query.fetchOne() != null;
  }
}
