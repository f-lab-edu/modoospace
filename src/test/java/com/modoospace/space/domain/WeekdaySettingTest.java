package com.modoospace.space.domain;

import java.time.DayOfWeek;
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
        List<WeekdaySetting> weekdaySettings = Arrays.asList(
            new WeekdaySetting(DayOfWeek.SATURDAY), new WeekdaySetting(DayOfWeek.TUESDAY));

        Collections.sort(weekdaySettings, Comparator.comparing(WeekdaySetting::getWeekday));

        System.out.println(weekdaySettings);
    }

}
