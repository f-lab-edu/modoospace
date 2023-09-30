package com.modoospace.alarm.controller.dto;

import com.modoospace.alarm.domain.AlarmType;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AlarmReadDto implements Serializable {

  @NotNull
  private Long id;

  @NotNull
  private Long reservationId;

  @NotNull
  private String message;

  @Builder
  public AlarmReadDto(Long id, Long reservationId, String facilityName, AlarmType alarmType) {
    this.id = id;
    this.reservationId = reservationId;
    this.message = facilityName + alarmType.getAlarmText();
  }
}
