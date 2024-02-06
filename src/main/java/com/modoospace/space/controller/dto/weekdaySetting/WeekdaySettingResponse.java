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
public class WeekdaySettingResponse {

  @NotNull
  private Long id;

  @NotNull
  private DayOfWeek weekday;

  public WeekdaySettingResponse(Long id, DayOfWeek weekday) {
    this.id = id;
    this.weekday = weekday;
  }

  public static WeekdaySettingResponse of(WeekdaySetting weekdaySetting) {
    return new WeekdaySettingResponse(weekdaySetting.getId(), weekdaySetting.getWeekday());
  }

  public static List<WeekdaySettingResponse> of(List<WeekdaySetting> weekdaySettings) {
    return weekdaySettings.stream()
        .map(WeekdaySettingResponse::of)
        .collect(Collectors.toList());
  }
}
