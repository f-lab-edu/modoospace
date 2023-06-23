package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modoospace.exception.ConflictingTimeException;
import com.modoospace.exception.InvalidTimeRangeException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeSettingTest {

  @DisplayName("시작시간은 종료시간보다 이후일 수 없다.")
  @Test
  public void TimeSetting_throwException_ifStartTimeAfterEndTime() {
    assertThatThrownBy(() -> TimeSetting.builder()
        .startTime(LocalTime.of(16, 0))
        .endTime(LocalTime.of(14, 59, 59))
        .build()).isInstanceOf(InvalidTimeRangeException.class);
  }

  @DisplayName("시간이 겹치는지 확인한다.")
  @Test
  public void verifyConflicting() {
    TimeSetting timeSetting = TimeSetting.builder()
        .startTime(LocalTime.of(9, 00))
        .endTime(LocalTime.of(14, 59, 59))
        .build();

    TimeSetting compareTimeSetting = TimeSetting.builder()
        .startTime(LocalTime.of(15, 00))
        .endTime(LocalTime.of(18, 59, 59))
        .build();

    timeSetting.verifyConflicting(compareTimeSetting);
  }

  @DisplayName("시간이 겹친다면 예외를 던진다.")
  @Test
  public void verifyConflicting_throwException_ifTimeOverlapping() {
    TimeSetting timeSetting = TimeSetting.builder()
        .startTime(LocalTime.of(10, 00))
        .endTime(LocalTime.of(18, 59, 59))
        .build();
    TimeSetting compareTimeSetting = TimeSetting.builder()
        .startTime(LocalTime.of(16, 00))
        .endTime(LocalTime.of(18, 59, 59))
        .build();

    assertThatThrownBy(() -> timeSetting.verifyConflicting(compareTimeSetting))
        .isInstanceOf(ConflictingTimeException.class);
  }

  @DisplayName("Time세팅값을 시작시간이 빠른 순으로 정렬한다.")
  @Test
  public void TimeSetting_Sort() {
    TimeSetting timeSetting = TimeSetting.builder()
        .startTime(LocalTime.of(14, 0))
        .endTime(LocalTime.of(18, 59, 59))
        .build();
    TimeSetting timeSetting2 = TimeSetting.builder()
        .startTime(LocalTime.of(9, 0))
        .endTime(LocalTime.of(18, 59, 59))
        .build();
    List<TimeSetting> timeSettings = Arrays.asList(timeSetting, timeSetting2);

    Collections.sort(timeSettings, Comparator.comparing(TimeSetting::getStartTime));

    System.out.println(timeSettings);
  }
}
