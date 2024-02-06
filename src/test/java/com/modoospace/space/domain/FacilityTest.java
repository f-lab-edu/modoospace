package com.modoospace.space.domain;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FacilityTest {

    @DisplayName("시설 생성 시 TimeSetting과 WeekSetting에 맞춰 오늘 날짜로 부터 3개월간의 스케줄데이터를 생성한다.")
    @Test
    public void createFacility_24HourOpen_ifNotSelectSetting() {

        List<TimeSetting> timeSettings = Arrays.asList(
            new TimeSetting(new TimeRange(9, 12)), new TimeSetting(new TimeRange(14, 20)));

        List<WeekdaySetting> weekdaySettings = Arrays.asList(
            new WeekdaySetting(DayOfWeek.WEDNESDAY), new WeekdaySetting(DayOfWeek.THURSDAY),
            new WeekdaySetting(DayOfWeek.FRIDAY), new WeekdaySetting(DayOfWeek.SATURDAY));

        Facility facility = Facility.builder()
            .name("테스트")
            .minUser(1)
            .maxUser(3)
            .timeSettings(new TimeSettings(timeSettings))
            .weekdaySettings(new WeekdaySettings(weekdaySettings))
            .build();

        System.out.println(facility.getSchedules());
    }
}
