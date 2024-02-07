package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.common.exception.ConflictingTimeException;
import com.modoospace.common.exception.InvalidTimeRangeException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeSettingTest {

    @DisplayName("시작시간은 종료시간보다 이후일 수 없다.")
    @Test
    public void TimeSetting_throwException_ifStartTimeAfterEndTime() {
        assertThatThrownBy(() -> new TimeSetting(new TimeRange(15, 9)))
            .isInstanceOf(InvalidTimeRangeException.class);
    }

    @DisplayName("시간이 겹치는지 확인한다.")
    @Test
    public void verifyConflicting() {
        TimeSetting timeSetting = new TimeSetting(new TimeRange(9, 15));
        TimeSetting tartgetTimeSetting = new TimeSetting(new TimeRange(15, 19));

        timeSetting.verifyConflicting(tartgetTimeSetting);
    }

    @DisplayName("시간이 겹친다면 예외를 던진다.")
    @Test
    public void verifyConflicting_throwException_ifTimeOverlapping() {
        TimeSetting timeSetting = new TimeSetting(new TimeRange(10, 19));
        TimeSetting tartgetTimeSetting = new TimeSetting(new TimeRange(16, 19));

        assertThatThrownBy(() -> timeSetting.verifyConflicting(tartgetTimeSetting))
            .isInstanceOf(ConflictingTimeException.class);
    }

    @DisplayName("시간이 연속적이라면 true를 반환한다.")
    @Test
    public void isContinuous() {
        TimeSetting timeSetting = new TimeSetting(new TimeRange(9, 15));
        TimeSetting tartgetTimeSetting = new TimeSetting(new TimeRange(15, 19));

        assertThat(timeSetting.isContinuous(tartgetTimeSetting)).isTrue();
    }

    @DisplayName("시간을 merge한다.")
    @Test
    public void merge() {
        TimeSetting timeSetting = new TimeSetting(new TimeRange(9, 15));
        TimeSetting tartgetTimeSetting = new TimeSetting(new TimeRange(15, 19));

        timeSetting.merge(tartgetTimeSetting);

        assertAll(
            () -> assertThat(timeSetting.getStartHour()).isEqualTo(9),
            () -> assertThat(timeSetting.getEndHour()).isEqualTo(19)
        );
    }

    @DisplayName("해당 날짜의 시설 스케줄을 생성한다.")
    @Test
    public void createSchedule() {
        TimeSetting timeSetting = new TimeSetting(new TimeRange(14, 19));
        LocalDate date = LocalDate.of(2022, 1, 1);

        Schedule retSchedule = timeSetting.createSchedule(date);

        assertAll(
            () -> assertThat(retSchedule.getDate()).isEqualTo(date),
            () -> assertThat(retSchedule.getStartHour()).isEqualTo(14),
            () -> assertThat(retSchedule.getEndHour()).isEqualTo(19)
        );
    }
}
