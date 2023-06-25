package com.modoospace.reservation.domain;

import com.modoospace.common.BaseTimeEntity;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.Space;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
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

  public Reservation(Long id, LocalDateTime reservationStart, LocalDateTime reservationEnd,
      ReservationStatus status, Facility facility, Member visitor) {
    this.id = id;
    this.reservationStart = reservationStart;
    this.reservationEnd = reservationEnd;
    this.status = status;
    this.facility = facility;
    this.visitor = visitor;
  }

  public Reservation approveReservation() {
    this.status = ReservationStatus.APPROVED;
    return this;
  }

  public void update(final Reservation updateReservation, Member loginMember) {
    validateReservationOwnership(loginMember);
    this.reservationStart = updateReservation.getReservationStart();
    this.reservationEnd = updateReservation.getReservationEnd();
    this.status = updateReservation.getStatus();
  }

  private void validateReservationOwnership(Member loginMember) {
    //TODO : 해당 공간의 호스트가 요청한게 맞는지 확인
    if(facility.getSpace().getHost() == loginMember){
      return;
    }
    loginMember.verifyRolePermission(Role.ADMIN);
  }
}
