package com.modoospace.alarm.controller.dto;

import com.modoospace.alarm.domain.Alarm;
import com.modoospace.alarm.domain.AlarmType;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AlarmReadDto {

  @NotNull
  private Long id;

  @NotNull
  private AlarmType alarmType;

  public AlarmReadDto(Long id, AlarmType alarmType) {
    this.id = id;
    this.alarmType = alarmType;
  }

  public static AlarmReadDto toDto(Alarm alarm) {
    return new AlarmReadDto(alarm.getId(), alarm.getAlarmType());
  }
}
