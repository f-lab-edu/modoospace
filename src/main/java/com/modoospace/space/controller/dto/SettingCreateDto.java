package com.modoospace.space.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modoospace.space.domain.Setting;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
public class SettingCreateDto {

  @Builder.Default
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
  private LocalTime startTime = LocalTime.of(0, 0, 0);

  @Builder.Default
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
  private LocalTime endTime = LocalTime.of(23, 59, 59);

  public SettingCreateDto(LocalTime startTime, LocalTime endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public Setting toEntity() {
    return Setting.builder()
        .startTime(startTime)
        .endTime(endTime)
        .build();
  }
}
