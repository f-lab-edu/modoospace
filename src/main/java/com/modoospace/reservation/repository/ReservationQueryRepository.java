package com.modoospace.reservation.repository;

import static com.modoospace.reservation.domain.QReservation.reservation;

import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.space.domain.Facility;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationQueryRepository {

  private final JPAQueryFactory jpaQueryFactory;

  public ReservationQueryRepository(JPAQueryFactory jpaQueryFactory) {
    this.jpaQueryFactory = jpaQueryFactory;
  }

  /**
   * 동일한 시설의 예약 중 겹치는 예약이 있는지 확인합니다.
   *
   * @param facility 시설
   * @param start    시작 시간
   * @param end      종료 시간
   * @return 겹치는 예약이 있는 경우 true, 없는 경우 false
   * @throws NotFoundEntityException 유효하지 않은 시설 ID가 입력된 경우
   */
  public Boolean isOverlappingReservation(Facility facility, LocalDateTime start,
      LocalDateTime end) {
    // 중복 예약 ID 목록 조회
    List<Long> reservationIds = findOverlappingReservation(facility, start, end);

    return !reservationIds.isEmpty();
  }

  private List<Long> findOverlappingReservation(Facility facility, LocalDateTime start,
      LocalDateTime end) {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(reservation.facility.eq(facility))
        .and(reservation.reservationStart.before(end))
        .and(reservation.reservationEnd.after(start));

    JPAQuery<Long> query = jpaQueryFactory
        .select(reservation.id)
        .from(reservation)
        .where(builder);

    return query.fetch();
  }
}
