package com.modoospace.alarm.controller.dto;

import com.modoospace.alarm.domain.Alarm;
import com.modoospace.alarm.domain.AlarmType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@NoArgsConstructor
public class AlarmResponse implements Serializable {

    @NotNull
    private Long id;

    @NotNull
    private Long reservationId;

    @NotNull
    private String message;

    @Builder
    public AlarmResponse(Long id, Long reservationId, String facilityName, AlarmType alarmType) {
        this.id = id;
        this.reservationId = reservationId;
        this.message = facilityName + alarmType.getAlarmText();
    }

    public static AlarmResponse of(Alarm alarm) {
        return AlarmResponse.builder()
                .id(alarm.getId())
                .reservationId(alarm.getReservationId())
                .facilityName(alarm.getFacilityName())
                .alarmType(alarm.getAlarmType())
                .build();
    }
}
