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

  private LocalDate testDate;

  @BeforeEach
  public void setUp() {
    testDate = LocalDate.now();
    if (testDate.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
      testDate = testDate.plusDays(1);
    }
    if (testDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
      testDate = testDate.plusDays(2);
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
            .startTime(LocalTime.of(9, 0, 0))
            .endTime(LocalTime.of(11, 59, 59))
            .build(),
        TimeSetting.builder()
            .startTime(LocalTime.of(13, 0, 0))
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
            .build());
    FacilitySchedules facilitySchedules = FacilitySchedules
        .createFacilitySchedules(new TimeSettings(timeSettings),
            new WeekdaySettings(weekdaySettings));

    LocalDateTime startDateTime = LocalDateTime.of(testDate, LocalTime.of(9, 0));
    LocalDateTime endDateTime = LocalDateTime.of(testDate, LocalTime.of(11, 59, 59));
    assertThat(facilitySchedules.isOpen(startDateTime, endDateTime)).isTrue();

    endDateTime = LocalDateTime.of(testDate, LocalTime.of(18, 0, 0));
    assertThat(facilitySchedules.isOpen(startDateTime, endDateTime)).isFalse();
  }
}
