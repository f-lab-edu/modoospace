package com.modoospace.space.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modoospace.space.domain.TimeSetting;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
public class TimeSettingCreateDto {

  @Builder.Default
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
  private LocalTime startTime = LocalTime.of(0, 0, 0);

  @Builder.Default
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
  private LocalTime endTime = LocalTime.of(23, 59, 59);

  public TimeSettingCreateDto(LocalTime startTime, LocalTime endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public TimeSetting toEntity() {
    return TimeSetting.builder()
        .startTime(startTime)
        .endTime(endTime)
        .build();
  }
}
