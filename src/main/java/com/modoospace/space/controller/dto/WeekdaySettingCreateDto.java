package com.modoospace.space.controller.dto;

import com.modoospace.space.domain.WeekdaySetting;
import java.time.DayOfWeek;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeekdaySettingCreateDto {

  private DayOfWeek weekday;

  public WeekdaySettingCreateDto(DayOfWeek weekday) {
    this.weekday = weekday;
  }

  public WeekdaySetting toEntity() {
    return WeekdaySetting.builder()
        .weekday(weekday)
        .build();
  }
}
