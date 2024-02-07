package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modoospace.common.exception.DuplicatedWeekdayException;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WeekdaySettingsTest {

    @DisplayName("시설 세팅값의 요일이 중복되면 예외를 던진다.")
    @Test
    public void WeekdaySettings_throwException_ifDuplicatingWeekday() {
        List<WeekdaySetting> weekdaySettings = Arrays.asList(
            new WeekdaySetting(DayOfWeek.WEDNESDAY), new WeekdaySetting(DayOfWeek.THURSDAY),
            new WeekdaySetting(DayOfWeek.WEDNESDAY));

        assertThatThrownBy(() -> new WeekdaySettings(weekdaySettings))
            .isInstanceOf(DuplicatedWeekdayException.class);
    }
}
