package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.common.exception.ConflictingTimeException;
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

    @DisplayName("스케줄 데이터끼리 시간이 겹치면 Exception을 던진다.")
    @Test
    public void verifyConflicting_throwException() {
        Schedule schedule = new Schedule(nowDate, new TimeRange(9, 15));
        Schedule targetSchedule1 = new Schedule(nowDate, new TimeRange(10, 20));
        Schedule targetSchedule2 = new Schedule(nowDate, new TimeRange(16, 20));
        Schedule targetSchedule3 = new Schedule(nowDate.plusDays(1), new TimeRange(10, 20));

        assertAll(
            () -> assertThatThrownBy(() -> schedule.verifyConflicting(targetSchedule1))
                .isInstanceOf(ConflictingTimeException.class),
            () -> schedule.verifyConflicting(targetSchedule2),
            () -> schedule.verifyConflicting(targetSchedule3)
        );
    }

    @DisplayName("스케줄 데이터끼리 시간이 이어진다면 true를 반환한다.")
    @Test
    public void isContinuous_returnTrue() {
        Schedule schedule = new Schedule(nowDate, new TimeRange(9, 15));
        Schedule targetSchedule1 = new Schedule(nowDate, new TimeRange(15, 20));
        Schedule targetSchedule2 = new Schedule(nowDate, new TimeRange(17, 20));

        assertAll(
            () -> assertThat(schedule.isContinuous(targetSchedule1)).isTrue(),
            () -> assertThat(schedule.isContinuous(targetSchedule2)).isFalse()
        );
    }

    @DisplayName("스케줄 데이터를 합친다.")
    @Test
    public void merge() {
        Schedule schedule = new Schedule(nowDate, new TimeRange(9, 15));
        Schedule targetSchedule1 = new Schedule(nowDate, new TimeRange(15, 20));

        schedule.merge(targetSchedule1);

        assertAll(
            () -> assertThat(schedule.getStartHour()).isEqualTo(9),
            () -> assertThat(schedule.getEndHour()).isEqualTo(20)
        );
    }

    @DisplayName("스케줄 데이터가 해당 시간(hour~hour+1)을 포함 하고 있다면 True를 반환한다.")
    @Test
    public void isBetween_returnTrue() {
        Schedule schedule = new Schedule(nowDate, new TimeRange(9, 18));

        assertAll(
            () -> assertThat(schedule.isBetween(9)).isTrue(),
            () -> assertThat(schedule.isBetween(16)).isTrue(),
            () -> assertThat(schedule.isBetween(18)).isFalse(),
            () -> assertThat(schedule.isBetween(20)).isFalse()
        );
    }
}
