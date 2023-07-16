package com.modoospace.space.controller.dto.weekdaySetting;

import com.modoospace.space.domain.WeekdaySetting;
import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeekdaySettingReadDto {

  @NotNull
  private Long id;

  @NotNull
  private DayOfWeek weekday;

  public WeekdaySettingReadDto(Long id, DayOfWeek weekday) {
    this.id = id;
    this.weekday = weekday;
  }

  public static WeekdaySettingReadDto toDto(WeekdaySetting weekdaySetting) {
    return new WeekdaySettingReadDto(weekdaySetting.getId(), weekdaySetting.getWeekday());
  }

  public static List<WeekdaySettingReadDto> toDtos(List<WeekdaySetting> weekdaySettings) {
    return weekdaySettings.stream()
        .map(WeekdaySettingReadDto::toDto)
        .collect(Collectors.toList());
  }
}
