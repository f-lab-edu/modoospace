package com.modoospace.reservation.domain;

import com.modoospace.common.BaseTimeEntity;
import com.modoospace.space.domain.Facility;
import com.modoospace.member.domain.Member;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

  @Id
  @GeneratedValue
  @Column(name = "reservation_id")
  private Long id;

  @Column(nullable = false)
  private LocalDateTime reservationStart; // 예약 시작 일시

  @Column(nullable = false)
  private LocalDateTime reservationEnd; // 예약 종료 일시

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReservationStatus status; // 상태

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facility_id")
  private Facility facility; // 시설

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "visitor_id")
  private Member visitor; // 방문자
}
