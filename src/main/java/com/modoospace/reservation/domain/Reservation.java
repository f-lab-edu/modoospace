package com.modoospace.reservation.domain;

import com.modoospace.common.BaseTimeEntity;
import com.modoospace.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityType;
import java.time.LocalDateTime;
import java.util.Objects;
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
import lombok.ToString;
import org.springframework.transaction.annotation.Transactional;

@ToString
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

  @Builder
  public Reservation(LocalDateTime reservationStart, LocalDateTime reservationEnd, Facility facility, Member visitor) {
    this.reservationStart = reservationStart;
    this.reservationEnd = reservationEnd;

    FacilityType facilityType = facility.getFacilityType();
    this.status = facilityType.getDefaultStatus();

    this.facility = facility;
    this.visitor = visitor;
  }

  public void approveReservation(Member loginMember) {
    verifyHostRole(loginMember);
    this.status = ReservationStatus.COMPLETED;
  }

  public void updateAsHost(final Reservation updateReservation, Member loginMember) {
    verifyHostRole(loginMember);
    this.reservationStart = updateReservation.getReservationStart();
    this.reservationEnd = updateReservation.getReservationEnd();
    this.status = updateReservation.getStatus();
  }

  public void verifyHostRole(Member loginMember) {
    if (facility.getSpace().getHost() == loginMember) {
      return;
    }
    loginMember.verifyRolePermission(Role.ADMIN);
  }

  @Transactional
  public void updateStatusToCanceled(Member loginMember) {
    verifySameVisitor(loginMember);
    this.status = ReservationStatus.CANCELED;
  }

  public void verifySameVisitor(Member loginMember) {
    if (!Objects.equals(visitor.getEmail(), loginMember.getEmail())) {
      throw new PermissionDeniedException();
    }
  }
}
