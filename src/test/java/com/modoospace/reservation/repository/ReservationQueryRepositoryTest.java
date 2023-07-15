package com.modoospace.reservation.repository;

import static com.modoospace.reservation.domain.QReservation.reservation;
import static org.assertj.core.api.Assertions.assertThat;

import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
public class ReservationQueryRepositoryTest {

  @Autowired
  private JPAQueryFactory jpaQueryFactory;

  @Autowired
  private ReservationQueryRepository reservationQueryRepository;

  @Autowired
  private ReservationRepository reservationRepository;
  @Test
  @DisplayName("기존 동일한 시설,시간에 예약이 없다면 빈 리스트를 반환한다.")
  public void findOverlappingReservationIds_Success() {
    LocalDateTime differentStart = LocalDateTime.of(2023, 7, 14, 3, 0);
    LocalDateTime differentEnd = LocalDateTime.of(2023, 7, 14, 3, 0).plusHours(2);
    List<Long> reservationIds = getReservationIds();

    Boolean isExist = reservationQueryRepository.existOverlappingReservation(reservationIds, differentStart, differentEnd);

    assertThat(isExist).isFalse();
  }

  @Test
  @DisplayName("기존 동일한 시설,시간에 예약이있다면 예약ID 리스트를 반환한다.")
  public void findOverlappingReservationIds() {
    // Given
    LocalDateTime requestStart = LocalDateTime.of(2023, 7, 14, 10, 0);
    LocalDateTime requestEnd = LocalDateTime.of(2023, 7, 14, 13, 59);
    List<Long> reservationIds = getReservationIds();

    BooleanExpression expression = overlappingReservation(reservationIds, requestStart, requestEnd);

    Integer fetchOne = jpaQueryFactory
        .selectOne()
        .from(reservation)
        .where(expression)
        .fetchFirst();

    assertThat(fetchOne).isGreaterThan(0);
  }

  private List<Long> getReservationIds() {
    List<Reservation> reservations = reservationRepository.findAll();
    return reservations.stream()
        .map(Reservation::getId)
        .collect(Collectors.toList());
  }

  private static BooleanExpression overlappingReservation(List<Long> reservationIds,
      LocalDateTime start, LocalDateTime end) {
    return reservation.id.in(reservationIds)
        .and(reservation.reservationStart.before(end))
        .and(reservation.reservationEnd.after(start));
  }
}
