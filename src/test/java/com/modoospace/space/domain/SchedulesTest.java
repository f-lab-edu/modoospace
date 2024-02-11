package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.common.exception.ConflictingTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchedulesTest {

    @DisplayName("TimeSettings과 WeekdaySettings으로 현재 날짜부터 3개월간의 데이터를 생성한다.")
    @Test
    public void createFacilitySchedules() {
        TimeSettings timeSettings = createTimeSetting(new TimeRange(0, 24));
        WeekdaySettings weekDaySetting = createWeekDaySetting(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        Schedules schedules = Schedules
            .create3MonthSchedules(timeSettings, weekDaySetting, YearMonth.now());

        System.out.println(schedules);
    }

    private TimeSettings createTimeSetting(TimeRange... timeRanges) {
        List<TimeSetting> timeSettings = Arrays.stream(timeRanges)
            .map(TimeSetting::new)
            .collect(Collectors.toList());
        return new TimeSettings(timeSettings);
    }

    private WeekdaySettings createWeekDaySetting(DayOfWeek... dayOfWeeks) {
        List<WeekdaySetting> weekdaySettings = Arrays.stream(dayOfWeeks)
            .map(WeekdaySetting::new)
            .collect(Collectors.toList());
        return new WeekdaySettings(weekdaySettings);
    }

    @DisplayName("시설 스케줄을 추가한다.")
    @Test
    public void addFacilitySchedule() {
        Schedules schedules = createSchedules(createNowDateSchedule(9, 12));
        Schedule addSchedule = createNowDateSchedule(13, 14);

        schedules.add(addSchedule);

        Schedule retSchedule = schedules.getSchedules().get(1);
        assertAll(
            () -> retSchedule.getStartHour().equals(13),
            () -> retSchedule.getEndHour().equals(14)
        );
    }

    @DisplayName("시설 스케줄을 추가한다. 하지만 범위가 연속적이므로 합쳐서 저장된다.")
    @Test
    public void addFacilitySchedule_Merge() {
        Schedules schedules = createSchedules(createNowDateSchedule(9, 12));
        Schedule addSchedule = createNowDateSchedule(12, 14);

        schedules.add(addSchedule);

        Schedule retSchedule = schedules.getSchedules().get(0);
        assertAll(
            () -> retSchedule.getStartHour().equals(9),
            () -> retSchedule.getEndHour().equals(14)
        );
    }

    @DisplayName("시설 스케줄을 추가한다. 하지만 범위가 연속적이므로 합쳐서 저장된다. (merge가 총 2번일어난다)")
    @Test
    public void addFacilitySchedule_Merge2() {
        Schedules schedules = createSchedules(createNowDateSchedule(9, 12),
            createNowDateSchedule(13, 18));
        Schedule addSchedule = createNowDateSchedule(12, 13);

        schedules.add(addSchedule);

        Schedule retSchedule = schedules.getSchedules().get(0);
        assertAll(
            () -> retSchedule.getStartHour().equals(9),
            () -> retSchedule.getEndHour().equals(18)
        );
    }

    @DisplayName("시설 스케줄을 추가 시 기존 스케줄과 겹친다면 예외를 던진다.")
    @Test
    public void addFacilitySchedule_throwException_ifConflict() {
        Schedules schedules = createSchedules(createNowDateSchedule(9, 12));
        Schedule addSchedule = createNowDateSchedule(11, 13);

        assertThatThrownBy(() -> schedules.add(addSchedule))
            .isInstanceOf(ConflictingTimeException.class);
    }

    @DisplayName("시설 스케줄을 업데이트 한다.")
    @Test
    public void updateFacilitySchedule() {
        Schedule schedule1 = createNowDateSchedule(9, 12);
        Schedule schedule2 = createNowDateSchedule(14, 18);
        Schedules schedules = createSchedules(schedule1, schedule2);
        Schedule updateSchedule = createNowDateSchedule(13, 18);

        schedules.update(updateSchedule, schedule2);

        Schedule retSchedule = schedules.getSchedules().get(0);
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
        Schedules schedules = createSchedules(schedule1, schedule2);
        Schedule updateSchedule = createNowDateSchedule(12, 18);

        schedules.update(updateSchedule, schedule2);

        Schedule retSchedule = schedules.getSchedules().get(0);
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
        Schedules schedules = createSchedules(schedule1, schedule2);
        Schedule updateSchedule = createNowDateSchedule(11, 18);

        assertThatThrownBy(() -> schedules.update(updateSchedule, schedule2))
            .isInstanceOf(ConflictingTimeException.class);
    }


    private Schedule createNowDateSchedule(Integer start, Integer end) {
        TimeRange timeRange = new TimeRange(start, end);
        return Schedule.builder()
            .date(LocalDate.now())
            .timeRange(timeRange)
            .build();
    }

    private Schedules createSchedules(Schedule... schedule) {
        List<Schedule> schedules = Arrays.stream(schedule)
            .collect(Collectors.toList());
        return new Schedules(schedules);
    }
}
