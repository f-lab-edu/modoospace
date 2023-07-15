package com.modoospace.reservation.repository;

import static com.modoospace.reservation.domain.QReservation.reservation;

import com.modoospace.exception.NotFoundEntityException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
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
   * @param facilityId 시설 ID
   * @param start      시작 시간
   * @param end        종료 시간
   * @return 겹치는 예약이 있는 경우 true, 없는 경우 false
   * @throws NotFoundEntityException 유효하지 않은 시설 ID가 입력된 경우
   */
  public Boolean isOverlappingReservation(Long facilityId, LocalDateTime start, LocalDateTime end) {
    if (facilityId == null || facilityId <= 0) {
      throw new NotFoundEntityException("시설", facilityId);
    }

    // 시설 ID로 예약 ID 목록 조회
    List<Long> reservationIds = findReservationIdsByFacilityId(facilityId);

    // 중복 예약 ID 목록 조회
    return existOverlappingReservation(reservationIds, start, end);
  }

  /**
   * 시설 ID로 예약 ID 목록을 조회합니다.
   *
   * @param facilityId 시설 ID
   * @return 예약 ID 목록
   */
  public List<Long> findReservationIdsByFacilityId(Long facilityId) {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(reservation.facility.id.eq(facilityId));

    JPAQuery<Long> query = jpaQueryFactory
        .select(reservation.id)
        .from(reservation)
        .where(builder);

    return query.fetch();
  }

  /**
   * 동일한 시간에 예약이 겹치는지 확인합니다.
   *
   * @param reservationIds 예약 ID 목록
   * @param start          시작 시간
   * @param end            종료 시간
   * @return 겹치는 예약이 있는 경우 true, 없는 경우 false
   */
  public Boolean existOverlappingReservation(List<Long> reservationIds, LocalDateTime start, LocalDateTime end) {
    if (reservationIds.isEmpty()) {
      return false;
    }

    BooleanExpression expression = createOverlappingReservationCondition(reservationIds, start, end);

    Integer fetchOne = jpaQueryFactory
        .selectOne()
        .from(reservation)
        .where(expression)
        .fetchFirst();
    return fetchOne != null;
  }

  private static BooleanExpression createOverlappingReservationCondition(List<Long> reservationIds, LocalDateTime start, LocalDateTime end) {
    return reservation.id.in(reservationIds)
        .and(reservation.reservationStart.before(end))
        .and(reservation.reservationEnd.after(start));
  }
}
