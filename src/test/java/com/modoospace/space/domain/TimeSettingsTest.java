package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modoospace.common.exception.ConflictingTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeSettingsTest {

    @DisplayName("시간 세팅값의 시간이 겹치면 예외를 던진다.")
    @Test
    public void TimeSettings_throwException_ifOverlappingTime() {
        ArrayList<TimeSetting> timeSettings = new ArrayList<>();
        timeSettings.add(createTimeSetting(10, 18));
        timeSettings.add(createTimeSetting(16, 22));

        assertThatThrownBy(() -> new TimeSettings(timeSettings))
            .isInstanceOf(ConflictingTimeException.class);
    }

    @DisplayName("시간 세팅값의 시간이 연속되면 합친다.")
    @Test
    public void TimeSettings_merge_ifContinuousTime() {
        ArrayList<TimeSetting> timeSettings = new ArrayList<>();
        timeSettings.add(createTimeSetting(10, 18));
        timeSettings.add(createTimeSetting(18, 22));

        TimeSettings retTimeSettings = new TimeSettings(timeSettings);

        assertThat(retTimeSettings.getTimeSettings()).hasSize(1);
    }

    @DisplayName("시간 세팅값에 맞게 스케줄데이터를 생성한다.")
    @Test
    public void createFacilitySchedules() {
        ArrayList<TimeSetting> timeSettings = new ArrayList<>();
        timeSettings.add(createTimeSetting(9, 12));
        timeSettings.add(createTimeSetting(13, 18));
        TimeSettings retTimeSettings = new TimeSettings(timeSettings);

        List<Schedule> facilitySchedules = retTimeSettings.createSchedules(LocalDate.now());

        assertThat(facilitySchedules).hasSize(2);
    }

    @DisplayName("시설 세팅값이 비었다면 true를 던진다.")
    @Test
    public void isEmpty_returnTrue() {
        TimeSettings timeSettings = new TimeSettings(new ArrayList<>());

        assertThat(timeSettings.isEmpty()).isTrue();
    }

    private TimeSetting createTimeSetting(Integer start, Integer end) {
        TimeRange timeRange = new TimeRange(start, end);
        return TimeSetting.builder()
            .timeRange(timeRange)
            .build();
    }
}
