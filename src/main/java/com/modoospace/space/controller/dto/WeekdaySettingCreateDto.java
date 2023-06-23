package com.modoospace.space.controller.dto;

import com.modoospace.space.domain.Weekday;
import com.modoospace.space.domain.WeekdaySetting;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeekdaySettingCreateDto {

  private Weekday weekday;

  public WeekdaySettingCreateDto(Weekday weekday) {
    this.weekday = weekday;
  }

  public WeekdaySetting toEntity() {
    return WeekdaySetting.builder()
        .weekday(weekday)
        .build();
  }
}
