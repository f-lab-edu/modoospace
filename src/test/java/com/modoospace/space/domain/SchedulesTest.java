package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.common.exception.ConflictingTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchedulesTest {

    private LocalDate nowDate;

    @BeforeEach
    public void setUp() {
        nowDate = LocalDate.now();
    }

    @DisplayName("TimeSettings과 WeekdaySettings으로 현재 날짜부터 3개월간의 데이터를 생성한다.")
    @Test
    public void createFacilitySchedules() {
        List<TimeSetting> timeSettings = Arrays.asList(
            TimeSetting.builder()
                .timeRange(new TimeRange(0, 24))
                .build());
        List<WeekdaySetting> weekdaySettings = Arrays.asList(
            WeekdaySetting.builder()
                .weekday(DayOfWeek.MONDAY)
                .build(),
            WeekdaySetting.builder()
                .weekday(DayOfWeek.TUESDAY)
                .build(),
            WeekdaySetting.builder()
                .weekday(DayOfWeek.WEDNESDAY)
                .build(),
            WeekdaySetting.builder()
                .weekday(DayOfWeek.THURSDAY)
                .build(),
            WeekdaySetting.builder()
                .weekday(DayOfWeek.FRIDAY)
                .build()
        );
        Schedules allDaySchedules = Schedules
            .create3MonthFacilitySchedules(new TimeSettings(timeSettings),
                new WeekdaySettings(weekdaySettings), YearMonth.now());

        System.out.println(allDaySchedules);
    }

    @DisplayName("시설 스케줄을 추가한다.")
    @Test
    public void addFacilitySchedule() {
        Schedule schedule = createNowDateSchedule(9, 12);
        Schedule createSchedule = createNowDateSchedule(13, 14);

        List<Schedule> schedules = new ArrayList<>();
        schedules.add(schedule);
        Schedule retSchedule = Schedules.addSchedule(schedules, createSchedule);

        assertAll(
            () -> retSchedule.getStartHour().equals(13),
            () -> retSchedule.getEndHour().equals(14)
        );
    }

    @DisplayName("시설 스케줄을 추가한다. 하지만 범위가 연속적이므로 합쳐서 저장된다.")
    @Test
    public void addFacilitySchedule_Merge() {
        Schedule schedule = createNowDateSchedule(9, 12);
        Schedule createSchedule = createNowDateSchedule(12, 14);

        List<Schedule> schedules = new ArrayList<>();
        schedules.add(schedule);
        Schedule retSchedule = Schedules.addSchedule(schedules, createSchedule);

        assertAll(
            () -> retSchedule.getStartHour().equals(9),
            () -> retSchedule.getEndHour().equals(14)
        );
    }

    @DisplayName("시설 스케줄을 추가한다. 하지만 범위가 연속적이므로 합쳐서 저장된다. (merge가 총 2번일어난다)")
    @Test
    public void addFacilitySchedule_Merge2() {
        Schedule schedule1 = createNowDateSchedule(9, 12);
        Schedule schedule2 = createNowDateSchedule(13, 18);
        Schedule createSchedule = createNowDateSchedule(12, 13);

        List<Schedule> schedules = new ArrayList<>();
        schedules.add(schedule1);
        schedules.add(schedule2);
        Schedule retSchedule = Schedules.addSchedule(schedules, createSchedule);

        assertAll(
            () -> retSchedule.getStartHour().equals(9),
            () -> retSchedule.getEndHour().equals(18)
        );
    }

    @DisplayName("시설 스케줄을 추가 시 기존 스케줄과 겹친다면 예외를 던진다.")
    @Test
    public void addFacilitySchedule_throwException_ifConflict() {
        Schedule schedule = createNowDateSchedule(9, 12);
        Schedule createSchedule = createNowDateSchedule(11, 13);

        List<Schedule> schedules = new ArrayList<>();
        schedules.add(schedule);
        assertThatThrownBy(() -> Schedules.addSchedule(schedules, createSchedule))
            .isInstanceOf(ConflictingTimeException.class);
    }

    @DisplayName("시설 스케줄을 업데이트 한다.")
    @Test
    public void updateFacilitySchedule() {
        Schedule schedule1 = createNowDateSchedule(9, 12);
        Schedule schedule2 = createNowDateSchedule(14, 18);
        Schedule updateSchedule = createNowDateSchedule(13, 18);

        List<Schedule> schedules = new ArrayList<>();
        schedules.add(schedule1);
        schedules.add(schedule2);
        Schedule retSchedule = Schedules.updateSchedule(schedules, updateSchedule, schedule2);

        assertAll(
            () -> retSchedule.getStartHour().equals(13),
            () -> retSchedule.getEndHour().equals(18)
        );
    }

    @DisplayName("시설 스케줄을 업데이트 한다. 하지만 범위가 연속적이므로 합쳐서 저장된다.")
    @Test
    public void updateFacilitySchedule_Merge() {
        Schedule schedule1 = createNowDateSchedule(9, 12);
        Schedule schedule2 = createNowDateSchedule(14, 18);
        Schedule updateSchedule = createNowDateSchedule(12, 18);

        List<Schedule> schedules = new ArrayList<>();
        schedules.add(schedule1);
        schedules.add(schedule2);
        Schedule retSchedule = Schedules.updateSchedule(schedules, updateSchedule, schedule2);

        assertAll(
            () -> retSchedule.getStartHour().equals(9),
            () -> retSchedule.getEndHour().equals(18)
        );
    }

    @DisplayName("시설 스케줄을 업데이트 시 기존 스케줄과 겹친다면 예외를 던진다.")
    @Test
    public void updateFacilitySchedule_throwException_ifConflict() {
        Schedule schedule1 = createNowDateSchedule(9, 12);
        Schedule schedule2 = createNowDateSchedule(14, 18);
        Schedule updateSchedule = createNowDateSchedule(11, 18);

        List<Schedule> schedules = new ArrayList<>();
        schedules.add(schedule1);
        schedules.add(schedule2);

        assertThatThrownBy(() -> Schedules.updateSchedule(schedules, updateSchedule, schedule2))
            .isInstanceOf(ConflictingTimeException.class);
    }

    private Schedule createNowDateSchedule(Integer start, Integer end) {
        TimeRange timeRange = new TimeRange(start, end);
        return Schedule.builder()
            .date(nowDate)
            .timeRange(timeRange)
            .build();
    }
}
