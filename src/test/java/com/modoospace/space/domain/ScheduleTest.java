package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScheduleTest {

    private LocalDate nowDate;

    @BeforeEach
    public void setup() {
        nowDate = LocalDate.now();
    }

    @DisplayName("같은 날짜의 스케줄 데이터면 true를 반환한다.")
    @Test
    public void isDateEqual_returnTrue() {
        Schedule schedule = createNowDateSchedule(9, 15);
        Schedule targetSchedule1 = createNowDateSchedule(10, 20);
        Schedule targetSchedule2 = createSchedule(nowDate.plusDays(1), 10, 20);

        assertAll(
            () -> assertThat(schedule.isDateEqual(targetSchedule1)).isTrue(),
            () -> assertThat(schedule.isDateEqual(targetSchedule2)).isFalse()
        );
    }

    @DisplayName("스케줄 데이터끼리 시간이 겹치면 true를 반환한다.")
    @Test
    public void isConflicting_returnTrue() {
        Schedule schedule = createNowDateSchedule(9, 15);
        Schedule targetSchedule1 = createNowDateSchedule(10, 20);
        Schedule targetSchedule2 = createNowDateSchedule(16, 20);
        Schedule targetSchedule3 = createSchedule(nowDate.plusDays(1), 10, 20);

        assertAll(
            () -> assertThat(schedule.isConflicting(targetSchedule1)).isTrue(),
            () -> assertThat(schedule.isConflicting(targetSchedule2)).isFalse(),
            () -> assertThat(schedule.isConflicting(targetSchedule3)).isFalse()
        );
    }

    @DisplayName("스케줄 데이터끼리 시간이 이어진다면 true를 반환한다.")
    @Test
    public void isContinuous_returnTrue() {
        Schedule schedule = createNowDateSchedule(9, 15);
        Schedule targetSchedule1 = createNowDateSchedule(15, 20);
        Schedule targetSchedule2 = createNowDateSchedule(17, 20);

        assertAll(
            () -> assertThat(schedule.isContinuous(targetSchedule1)).isTrue(),
            () -> assertThat(schedule.isContinuous(targetSchedule2)).isFalse()
        );
    }

    @DisplayName("스케줄 데이터를 합친다.")
    @Test
    public void merge() {
        Schedule schedule = createNowDateSchedule(9, 15);
        Schedule targetSchedule1 = createNowDateSchedule(15, 20);

        schedule.merge(targetSchedule1);

        assertAll(
            () -> assertThat(schedule.getStartHour()).isEqualTo(9),
            () -> assertThat(schedule.getEndHour()).isEqualTo(20)
        );
    }

    @DisplayName("스케줄 데이터가 24시간 범위인지 체크한다.")
    @Test
    public void is24TimeRange_returnTrue() {
        Schedule schedule24 = createNowDateSchedule(0, 24);
        Schedule schedule20 = createNowDateSchedule(0, 20);

        assertAll(
            () -> assertThat(schedule24.is24TimeRange()).isTrue(),
            () -> assertThat(schedule20.is24TimeRange()).isFalse()
        );
    }

    @DisplayName("스케줄 데이터가 해당 시간(hour~hour+1)을 포함 하고 있다면 True를 반환한다.")
    @Test
    public void isBetween_returnTrue() {
        Schedule schedule = createNowDateSchedule(9, 18);

        assertAll(
            () -> assertThat(schedule.isBetween(9)).isTrue(),
            () -> assertThat(schedule.isBetween(16)).isTrue(),
            () -> assertThat(schedule.isBetween(18)).isFalse(),
            () -> assertThat(schedule.isBetween(20)).isFalse()
        );
    }

    private Schedule createNowDateSchedule(Integer start, Integer end) {
        TimeRange timeRange = new TimeRange(start, end);
        return Schedule.builder()
            .date(nowDate)
            .timeRange(timeRange)
            .build();
    }

    private Schedule createSchedule(LocalDate date, Integer start, Integer end) {
        TimeRange timeRange = new TimeRange(start, end);
        return Schedule.builder()
            .date(date)
            .timeRange(timeRange)
            .build();
    }
}
