package com.modoospace.reservation.domain;

import com.modoospace.common.BaseTimeEntity;
import com.modoospace.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityType;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
  public Reservation(LocalDateTime reservationStart, LocalDateTime reservationEnd,
      Facility facility, Member visitor) {
    this.reservationStart = reservationStart;
    this.reservationEnd = reservationEnd;

    FacilityType facilityType = facility.getFacilityType();
    this.status = facilityType.getDefaultStatus();

    this.facility = facility;
    this.visitor = visitor;
  }

  public void approveReservation(Member loginMember) {
    verifyManagementPermission(loginMember);
    this.status = ReservationStatus.COMPLETED;
  }

  public void updateAsHost(final Reservation updateReservation, Member loginMember) {
    verifyManagementPermission(loginMember);
    this.reservationStart = updateReservation.getReservationStart();
    this.reservationEnd = updateReservation.getReservationEnd();
    this.status = updateReservation.getStatus();
  }

  public void cancelAsVisitor(Member loginMember) {
    verifySameVisitor(loginMember);
    this.status = ReservationStatus.CANCELED;
  }

  public void verifyReservationAccess(Member loginMember) {
    if (loginMember.isSameRole(Role.VISITOR)) {
      verifySameVisitor(loginMember);
      return;
    }
    verifyManagementPermission(loginMember);
  }

  public void verifySameVisitor(Member loginMember) {
    if (visitor != loginMember) {
      throw new PermissionDeniedException();
    }
  }

  public void verifyManagementPermission(Member loginMember) {
    facility.verifyManagementPermission(loginMember);
  }

  public boolean isReservationBetween(LocalTime time) {
    LocalTime startTime = reservationStart.toLocalTime();
    LocalTime endTime = reservationEnd.toLocalTime();

    return (startTime.isBefore(time) || startTime.equals(time))
        && (endTime.isAfter(time) || endTime.equals(time));
  }

  public Member getHost() {
    return this.facility.getSpace().getHost();
  }
}
