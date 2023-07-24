package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modoospace.common.exception.ConflictingTimeException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeSettingsTest {

  @DisplayName("시설 세팅값의 시간이 겹치면 예외를 던진다.")
  @Test
  public void TimeSettings_throwException_ifOverlappingTime() {
    List<TimeSetting> timeSettings = Arrays.asList(TimeSetting.builder()
            .startTime(LocalTime.of(10, 00))
            .endTime(LocalTime.of(17, 59, 59))
            .build(),
        TimeSetting.builder()
            .startTime(LocalTime.of(16, 00))
            .endTime(LocalTime.of(21, 59, 59))
            .build());

    assertThatThrownBy(() -> new TimeSettings(timeSettings))
        .isInstanceOf(ConflictingTimeException.class);
  }
}
