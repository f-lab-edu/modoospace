package com.modoospace.alarm.controller.dto;

import com.modoospace.alarm.domain.Alarm;
import com.modoospace.alarm.domain.AlarmType;
import com.modoospace.reservation.domain.Reservation;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AlarmEvent {

    @NotNull
    private String email;

    @NotNull
    private Long reservationId;

    @NotNull
    private String facilityName;

    @NotNull
    private AlarmType alarmType;

    @Builder
    public AlarmEvent(String email, Long reservationId, String facilityName, AlarmType alarmType) {
        this.email = email;
        this.reservationId = reservationId;
        this.facilityName = facilityName;
        this.alarmType = alarmType;
    }

    public static AlarmEvent toNewReservationAlarm(Reservation reservation) {
        return AlarmEvent.builder()
            .email(reservation.getHost().getEmail())
            .reservationId(reservation.getId())
            .facilityName(reservation.getFacility().getName())
            .alarmType(AlarmType.NEW_RESERVATION)
            .build();
    }

    public static AlarmEvent toApprovedReservationAlarm(Reservation reservation) {
        return AlarmEvent.builder()
            .email(reservation.getVisitor().getEmail())
            .reservationId(reservation.getId())
            .facilityName(reservation.getFacility().getName())
            .alarmType(AlarmType.APPROVED_RESERVATION)
            .build();
    }

    public static AlarmEvent toCancelReservationAlarm(Reservation reservation) {
        return AlarmEvent.builder()
            .email(reservation.getHost().getEmail())
            .reservationId(reservation.getId())
            .facilityName(reservation.getFacility().getName())
            .alarmType(AlarmType.CANCELED_RESERVATION)
            .build();
    }

    public Alarm toEntity() {
        return Alarm.builder()
            .email(email)
            .reservationId(reservationId)
            .facilityName(facilityName)
            .alarmType(alarmType)
            .build();
    }
}
