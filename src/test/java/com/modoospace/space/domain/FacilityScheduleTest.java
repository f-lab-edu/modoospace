package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FacilityScheduleTest {

  @DisplayName("해당 범위에 스케줄 데이터가 포함 되고 있다면 True를 반환한다.")
  @Test
  public void isIncludedTimeRange_returnTrue() {
    FacilitySchedule facilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0, 0)))
        .endDateTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 59, 59)))
        .build();

    LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0, 0));
    LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 59, 59));
    assertThat(facilitySchedule.isIncludedTimeRange(startDateTime, endDateTime)).isTrue();

    startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0, 0));
    endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 59, 59));
    assertThat(facilitySchedule.isIncludedTimeRange(startDateTime, endDateTime)).isTrue();

    startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 0, 0));
    endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 59, 59));
    assertThat(facilitySchedule.isIncludedTimeRange(startDateTime, endDateTime)).isTrue();

    startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 0, 0));
    endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 59, 59));
    assertThat(facilitySchedule.isIncludedTimeRange(startDateTime, endDateTime)).isTrue();

  }

  @DisplayName("해당 범위에 스케줄 데이터가 포함 되고 있지 않다면 False를 반환한다.")
  @Test
  public void isIncludedTimeRange_returnFale() {
    FacilitySchedule facilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0, 0)))
        .endDateTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 59, 59)))
        .build();

    LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(6, 0, 0));
    LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 59, 59));
    assertThat(facilitySchedule.isIncludedTimeRange(startDateTime, endDateTime)).isFalse();

    startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(15, 0, 0));
    endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 59, 59));
    assertThat(facilitySchedule.isIncludedTimeRange(startDateTime, endDateTime)).isFalse();
  }

  @DisplayName("스케줄 데이터가 해당 범위를 포함 하고 있다면 True를 반환한다.")
  @Test
  public void isIncludingTimeRange_returnTrue() {
    FacilitySchedule facilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0, 0)))
        .endDateTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 59, 59)))
        .build();

    LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0, 0));
    LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 59, 59));
    assertThat(facilitySchedule.isIncludingTimeRange(startDateTime, endDateTime)).isTrue();

    startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 0, 0));
    endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(13, 59, 59));
    assertThat(facilitySchedule.isIncludingTimeRange(startDateTime, endDateTime)).isTrue();
  }

  @DisplayName("스케줄 데이터가 해당 범위를 포함 하고 있지 않다면 False를 반환한다.")
  @Test
  public void isIncludingTimeRange_returnFalse() {
    FacilitySchedule facilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0, 0)))
        .endDateTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 59, 59)))
        .build();

    LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0, 0));
    LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(15, 0, 0));
    assertThat(facilitySchedule.isIncludingTimeRange(startDateTime, endDateTime)).isFalse();

    startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 59, 59));
    endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 59, 59));
    assertThat(facilitySchedule.isIncludingTimeRange(startDateTime, endDateTime)).isFalse();
  }

  @DisplayName("스케줄 데이터가 24시간 범위를 가지면 True를 반환한다.")
  @Test
  public void is24TimeRange() {
    FacilitySchedule facilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0)))
        .endDateTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59)))
        .build();

    assertThat(facilitySchedule.is24TimeRange()).isTrue();
  }
}
