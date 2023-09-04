package com.modoospace.alarm.controller.dto;

import com.modoospace.alarm.domain.Alarm;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AlarmReadDto {

  @NotNull
  private Long id;

  @NotNull
  private Long reservationId;

  @NotNull
  private String message;

  @Builder
  public AlarmReadDto(Long id, Long reservationId, String message) {
    this.id = id;
    this.reservationId = reservationId;
    this.message = message;
  }

  public static AlarmReadDto toDto(Alarm alarm) {
    return AlarmReadDto.builder()
        .id(alarm.getId())
        .reservationId(alarm.getReservationId())
        .message(alarm.getAlarmMessage())
        .build();
  }
}
