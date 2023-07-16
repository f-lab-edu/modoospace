package com.modoospace.reservation.domain;

import com.modoospace.member.domain.Member;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  List<Reservation> findByVisitor(Member visitor);

  List<Reservation> findByFacilitySpaceHost(Member host);

  /**
   * 예약 시작 시간 범위, 예약 상태 및 시설 ID를 기준으로 예약을 조회합니다.
   *
   * @param reservationStart  예약 시작 시간 범위의 시작 값
   * @param reservationStart2 예약 시작 시간 범위의 끝 값
   * @param status            예약 상태 리스트
   * @param facility_id       시설 ID
   * @return 예약 리스트
   */
  List<Reservation> findByReservationStartBetweenAndStatusInAndFacility_Id(
      LocalDateTime reservationStart, LocalDateTime reservationStart2,
      Collection<ReservationStatus> status, Long facility_id);
}
