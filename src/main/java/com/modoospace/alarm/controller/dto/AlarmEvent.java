package com.modoospace.alarm.controller.dto;

import com.modoospace.alarm.domain.Alarm;
import com.modoospace.alarm.domain.AlarmType;
import com.modoospace.member.domain.Member;
import com.modoospace.reservation.domain.Reservation;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AlarmEvent {

  @NotNull
  private Long memberId;

  @NotNull
  private Long reservationId;

  @NotNull
  private AlarmType alarmType;

  @Builder
  public AlarmEvent(Long memberId, Long reservationId, AlarmType alarmType) {
    this.memberId = memberId;
    this.reservationId = reservationId;
    this.alarmType = alarmType;
  }

  public static AlarmEvent toNewReservationAlarm(Reservation reservation) {
    return AlarmEvent.builder()
        .memberId(reservation.getHost().getId())
        .reservationId(reservation.getId())
        .alarmType(AlarmType.NEW_RESERVATION)
        .build();
  }

  public static AlarmEvent toApprovedReservationAlarm(Reservation reservation) {
    return AlarmEvent.builder()
        .memberId(reservation.getVisitor().getId())
        .reservationId(reservation.getId())
        .alarmType(AlarmType.APPROVED_RESERVATION)
        .build();
  }

  public static AlarmEvent toCancelReservationAlarm(Reservation reservation) {
    return AlarmEvent.builder()
        .memberId(reservation.getHost().getId())
        .reservationId(reservation.getId())
        .alarmType(AlarmType.CANCELED_RESERVATION)
        .build();
  }

  public Alarm toEntity(Member member, Reservation reservation) {
    return Alarm.builder()
        .member(member)
        .reservation(reservation)
        .alarmType(alarmType)
        .build();
  }
}
