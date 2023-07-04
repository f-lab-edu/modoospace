package com.modoospace.space.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FacilityTest {

  @DisplayName("시설 생성 시 TimeSetting과 WeekSetting에 맞춰 오늘 날짜로 부터 3개월간의 스케줄데이터를 생성한다.")
  @Test
  public void createFacility_24HourOpen_ifNotSelectSetting() {
    List<TimeSetting> timeSettings = Arrays.asList(TimeSetting.builder()
            .startTime(LocalTime.of(9, 00))
            .endTime(LocalTime.of(11, 59, 59))
            .build(),
        TimeSetting.builder()
            .startTime(LocalTime.of(14, 00))
            .endTime(LocalTime.of(19, 59, 59))
            .build());

    List<WeekdaySetting> weekdaySettings = Arrays.asList(
        WeekdaySetting.builder()
            .weekday(DayOfWeek.WEDNESDAY)
            .build(),
        WeekdaySetting.builder()
            .weekday(DayOfWeek.THURSDAY)
            .build(),
        WeekdaySetting.builder()
            .weekday(DayOfWeek.FRIDAY)
            .build(),
        WeekdaySetting.builder()
            .weekday(DayOfWeek.SATURDAY)
            .build());

    Facility facility = Facility.builder()
        .timeSettings(new TimeSettings(timeSettings))
        .weekdaySettings(new WeekdaySettings(weekdaySettings))
        .build();

    System.out.println(facility.getFacilitySchedules());
  }
}
