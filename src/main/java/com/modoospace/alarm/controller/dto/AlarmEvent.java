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

    public static AlarmEvent ofNewReservationAlarm(Reservation reservation) {
        return AlarmEvent.builder()
            .email(reservation.getHost().getEmail())
            .reservationId(reservation.getId())
            .facilityName(reservation.getFacility().getFacilityName())
            .alarmType(AlarmType.NEW_RESERVATION)
            .build();
    }

    public static AlarmEvent ofApprovedReservationAlarm(Reservation reservation) {
        return AlarmEvent.builder()
            .email(reservation.getVisitor().getEmail())
            .reservationId(reservation.getId())
            .facilityName(reservation.getFacility().getFacilityName())
            .alarmType(AlarmType.APPROVED_RESERVATION)
            .build();
    }

    public static AlarmEvent ofCancelReservationAlarm(Reservation reservation) {
        return AlarmEvent.builder()
            .email(reservation.getHost().getEmail())
            .reservationId(reservation.getId())
            .facilityName(reservation.getFacility().getFacilityName())
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
