package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FacilitySchedulesTest {

  private LocalDate nowDate;

  @BeforeEach
  public void setUp() {
    nowDate = LocalDate.now();
    if (nowDate.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
      nowDate = nowDate.plusDays(1);
    }
    if (nowDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
      nowDate = nowDate.plusDays(2);
    }
  }

  @DisplayName("TimeSettings과 WeekdaySettings으로 현재 날짜부터 3개월간의 데이터를 생성한다.")
  @Test
  public void createFacilitySchedules() {
    List<TimeSetting> timeSettings = Arrays.asList(
        TimeSetting.builder()
            .startTime(LocalTime.of(9, 0, 0))
            .endTime(LocalTime.of(17, 59, 59))
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
            .build(),
        WeekdaySetting.builder()
            .weekday(DayOfWeek.SATURDAY)
            .build(),
        WeekdaySetting.builder()
            .weekday(DayOfWeek.SUNDAY)
            .build());
    FacilitySchedules facilitySchedules = FacilitySchedules
        .createFacilitySchedules(new TimeSettings(timeSettings),
            new WeekdaySettings(weekdaySettings));

    System.out.println(facilitySchedules.getFacilitySchedules());
  }

  @DisplayName("해당 시간범위에 시설이 Open했는지 여부를 반환한다.")
  @Test
  public void isOpen() {
    List<TimeSetting> timeSettings = Arrays.asList(
        TimeSetting.builder()
            .startTime(LocalTime.of(0, 0, 0))
            .endTime(LocalTime.of(23, 59, 59))
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
            .build(),
        WeekdaySetting.builder()
            .weekday(DayOfWeek.SATURDAY)
            .build(),
        WeekdaySetting.builder()
            .weekday(DayOfWeek.SUNDAY)
            .build());
    FacilitySchedules facilitySchedules = FacilitySchedules
        .createFacilitySchedules(new TimeSettings(timeSettings),
            new WeekdaySettings(weekdaySettings));

    LocalDateTime startDateTime = LocalDateTime.of(nowDate, LocalTime.of(9, 0, 0));
    LocalDateTime endDateTime = LocalDateTime.of(nowDate, LocalTime.of(11, 59, 59));
    assertThat(facilitySchedules.isOpen(startDateTime, endDateTime)).isTrue();

    startDateTime = LocalDateTime.of(nowDate, LocalTime.of(0, 0, 0));
    endDateTime = LocalDateTime.of(nowDate.plusDays(6), LocalTime.of(23, 59, 59));
    assertThat(facilitySchedules.isOpen(startDateTime, endDateTime)).isTrue();
  }

  @DisplayName("해당 시간범위에 시설이 Open했는지 여부를 반환한다. (날짜의 시간범위가 분리된 case 테스트)")
  @Test
  public void isOpen_ifWeekdaySettingUpdate() {
    List<TimeSetting> timeSettings = Arrays.asList(
        TimeSetting.builder()
            .startTime(LocalTime.of(0, 0, 0))
            .endTime(LocalTime.of(23, 59, 59))
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
            .build(),
        WeekdaySetting.builder()
            .weekday(DayOfWeek.SATURDAY)
            .build(),
        WeekdaySetting.builder()
            .weekday(DayOfWeek.SUNDAY)
            .build());
    FacilitySchedules facilitySchedules = FacilitySchedules
        .createFacilitySchedules(new TimeSettings(timeSettings),
            new WeekdaySettings(weekdaySettings));
    FacilitySchedule updateFacilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(nowDate.plusDays(1), LocalTime.of(0, 0, 0)))
        .endDateTime(LocalDateTime.of(nowDate.plusDays(1), LocalTime.of(17, 59, 59)))
        .build(); // 오늘날짜 + 1 스케줄 데이터를 업데이트한다. (0시 ~ 18시로 변경)
    FacilitySchedule facilitySchedule = facilitySchedules.getFacilitySchedules().get(nowDate.getDayOfMonth());
    facilitySchedule.update(updateFacilitySchedule);

    LocalDateTime startDateTime = LocalDateTime.of(nowDate, LocalTime.of(9, 0, 0));
    LocalDateTime endDateTime = LocalDateTime.of(nowDate.plusDays(1), LocalTime.of(17, 59, 59));
    assertThat(facilitySchedules.isOpen(startDateTime, endDateTime)).isTrue();

    endDateTime = LocalDateTime.of(nowDate.plusDays(2), LocalTime.of(17, 59, 59));
    assertThat(facilitySchedules.isOpen(startDateTime, endDateTime)).isFalse();
  }
}
