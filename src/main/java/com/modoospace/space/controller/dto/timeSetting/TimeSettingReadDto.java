package com.modoospace.space.controller.dto.timeSetting;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modoospace.common.DateFormatManager;
import com.modoospace.space.domain.TimeSetting;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TimeSettingReadDto {

  @NotNull
  private Long id;

  @NotNull
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatManager.TIME_FORMAT)
  private LocalTime startTime;

  @NotNull
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatManager.TIME_FORMAT)
  private LocalTime endTime;

  @Builder
  public TimeSettingReadDto(Long id, LocalTime startTime, LocalTime endTime) {
    this.id = id;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public static TimeSettingReadDto toDto(TimeSetting timeSetting) {
    return TimeSettingReadDto.builder()
        .id(timeSetting.getId())
        .startTime(timeSetting.getStartTime())
        .endTime(timeSetting.getEndTime())
        .build();
  }

  public static List<TimeSettingReadDto> toDtos(List<TimeSetting> timeSettings) {
    return timeSettings.stream()
        .map(TimeSettingReadDto::toDto)
        .collect(Collectors.toList());
  }
}
