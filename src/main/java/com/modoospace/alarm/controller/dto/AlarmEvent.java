package com.modoospace.alarm.controller.dto;

import com.modoospace.alarm.domain.AlarmType;
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
}
