package com.modoospace.space.controller.dto.weekdaySetting;

import com.modoospace.space.domain.WeekdaySetting;
import java.time.DayOfWeek;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeekdaySettingCreateRequest {

  private DayOfWeek weekday;

  public WeekdaySettingCreateRequest(DayOfWeek weekday) {
    this.weekday = weekday;
  }

  public WeekdaySetting toEntity() {
    return new WeekdaySetting(weekday);
  }
}
