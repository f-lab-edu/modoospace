package com.modoospace.space.domain;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WeekdaySettingTest {

  @DisplayName("Weekday세팅값을 요일 순으로 정렬한다.")
  @Test
  public void WeekdaySetting_Sort() {
    WeekdaySetting weekdaySetting1 = WeekdaySetting.builder()
        .weekday(Weekday.SAT)
        .build();
    WeekdaySetting weekdaySetting2 = WeekdaySetting.builder()
        .weekday(Weekday.TUE)
        .build();
    List<WeekdaySetting> weekdaySettings = Arrays.asList(weekdaySetting1, weekdaySetting2);

    Collections.sort(weekdaySettings, Comparator.comparing(WeekdaySetting::getWeekday));

    System.out.println(weekdaySettings);
  }

}
