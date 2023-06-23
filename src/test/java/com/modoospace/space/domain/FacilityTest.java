package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modoospace.exception.ConflictingTimeException;
import com.modoospace.exception.DuplicatedWeekdayException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FacilityTest {

  @DisplayName("시설 세팅값의 요일이 중복되면 예외를 던진다.")
  @Test
  public void Facility_throwException_ifDuplicatingWeekday() {
    List<WeekdaySetting> weekdaySettings = Arrays.asList(
        WeekdaySetting.builder()
            .weekday(Weekday.SAT)
            .build(),
        WeekdaySetting.builder()
            .weekday(Weekday.WED)
            .build(),
        WeekdaySetting.builder()
            .weekday(Weekday.WED)
            .build());

    assertThatThrownBy(() -> Facility.builder()
        .weekdaySettings(weekdaySettings)
        .build())
        .isInstanceOf(DuplicatedWeekdayException.class);
  }

  @DisplayName("시설 세팅값의 시간이 겹치면 예외를 던진다.")
  @Test
  public void Facility_throwException_ifOverlappingTime() {
    List<TimeSetting> timeSettings = Arrays.asList(TimeSetting.builder()
            .startTime(LocalTime.of(10, 00))
            .endTime(LocalTime.of(17, 59, 59))
            .build(),
        TimeSetting.builder()
            .startTime(LocalTime.of(16, 00))
            .endTime(LocalTime.of(21, 59, 59))
            .build());

    assertThatThrownBy(() -> Facility.builder()
        .timeSettings(timeSettings)
        .build())
        .isInstanceOf(ConflictingTimeException.class);
  }

}
