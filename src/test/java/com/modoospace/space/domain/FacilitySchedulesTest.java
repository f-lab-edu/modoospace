package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.exception.ConflictingTimeException;
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

  private FacilitySchedules facilitySchedules;

  private FacilitySchedules allDayFacilitySchedules;

  @BeforeEach
  public void setUp() {
    nowDate = LocalDate.now();
    if (nowDate.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
      nowDate = nowDate.plusDays(1);
    }
    if (nowDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
      nowDate = nowDate.plusDays(2);
    }

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
    facilitySchedules = FacilitySchedules
        .createFacilitySchedules(new TimeSettings(timeSettings),
            new WeekdaySettings(weekdaySettings));

    List<TimeSetting> timeSettings24 = Arrays.asList(
        TimeSetting.builder()
            .startTime(LocalTime.of(0, 0, 0))
            .endTime(LocalTime.of(23, 59, 59))
            .build());
    List<WeekdaySetting> weekdaySettingsAll = Arrays.asList(
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
    allDayFacilitySchedules = FacilitySchedules
        .createFacilitySchedules(new TimeSettings(timeSettings24),
            new WeekdaySettings(weekdaySettingsAll));
  }

  @DisplayName("TimeSettings과 WeekdaySettings으로 현재 날짜부터 3개월간의 데이터를 생성한다.")
  @Test
  public void createFacilitySchedules() {
    System.out.println(facilitySchedules);
  }

  @DisplayName("해당 시간범위에 시설이 Open했는지 여부를 반환한다.")
  @Test
  public void isOpen() {
    LocalDateTime startDateTime = LocalDateTime.of(nowDate, LocalTime.of(9, 0, 0));
    LocalDateTime endDateTime = LocalDateTime.of(nowDate, LocalTime.of(11, 59, 59));
    assertThat(allDayFacilitySchedules.isOpen(startDateTime, endDateTime)).isTrue();

    startDateTime = LocalDateTime.of(nowDate, LocalTime.of(0, 0, 0));
    endDateTime = LocalDateTime.of(nowDate.plusDays(6), LocalTime.of(23, 59, 59));
    assertThat(allDayFacilitySchedules.isOpen(startDateTime, endDateTime)).isTrue();
  }

  @DisplayName("해당 시간범위에 시설이 Open했는지 여부를 반환한다. (날짜의 시간범위가 분리된 case 테스트)")
  @Test
  public void isOpen_ifWeekdaySettingUpdate() {
    FacilitySchedule updateFacilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(nowDate.plusDays(1), LocalTime.of(0, 0, 0)))
        .endDateTime(LocalDateTime.of(nowDate.plusDays(1), LocalTime.of(17, 59, 59)))
        .build(); // 오늘날짜 + 1 스케줄 데이터를 업데이트한다. (0시 ~ 18시로 변경)
    FacilitySchedule facilitySchedule = allDayFacilitySchedules.getFacilitySchedules()
        .get(nowDate.getDayOfMonth());
    facilitySchedule.update(updateFacilitySchedule);

    LocalDateTime startDateTime = LocalDateTime.of(nowDate, LocalTime.of(9, 0, 0));
    LocalDateTime endDateTime = LocalDateTime.of(nowDate.plusDays(1), LocalTime.of(17, 59, 59));
    assertThat(allDayFacilitySchedules.isOpen(startDateTime, endDateTime)).isTrue();

    endDateTime = LocalDateTime.of(nowDate.plusDays(2), LocalTime.of(17, 59, 59));
    assertThat(allDayFacilitySchedules.isOpen(startDateTime, endDateTime)).isFalse();
  }

  @DisplayName("시설 스케줄을 추가한다.")
  @Test
  public void addFacilitySchedule() {
    FacilitySchedule createFacilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(nowDate, LocalTime.of(19, 0, 0)))
        .endDateTime(LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59)))
        .build();
    facilitySchedules.addFacilitySchedule(createFacilitySchedule);

    List<FacilitySchedule> retFacilitySchedules = facilitySchedules
        .isEqualsLocalDate(createFacilitySchedule);
    assertAll(
        () -> retFacilitySchedules.get(0).isStartTimeEquals(LocalTime.of(9, 0, 0)),
        () -> retFacilitySchedules.get(0).isEndTimeEquals(LocalTime.of(17, 59, 59)),
        () -> retFacilitySchedules.get(1).isStartTimeEquals(LocalTime.of(19, 0, 0)),
        () -> retFacilitySchedules.get(1).isEndTimeEquals(LocalTime.of(23, 59, 59))
    );
  }

  @DisplayName("시설 스케줄을 추가한다. 하지만 범위가 연속적이므로 합쳐서 저장된다.")
  @Test
  public void addFacilitySchedule_Merge() {
    FacilitySchedule createFacilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(nowDate, LocalTime.of(18, 0, 0)))
        .endDateTime(LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59)))
        .build();
    facilitySchedules.addFacilitySchedule(createFacilitySchedule);

    List<FacilitySchedule> retFacilitySchedules = facilitySchedules
        .isEqualsLocalDate(createFacilitySchedule);
    assertAll(
        () -> retFacilitySchedules.get(0).isStartTimeEquals(LocalTime.of(0, 0, 0)),
        () -> retFacilitySchedules.get(0).isEndTimeEquals(LocalTime.of(23, 59, 59))
    );
  }

  @DisplayName("시설 스케줄을 추가 시 기존 스케줄과 겹친다면 예외를 던진다.")
  @Test
  public void addFacilitySchedule_throwException_ifConflict() {
    FacilitySchedule createFacilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(nowDate, LocalTime.of(16, 0, 0)))
        .endDateTime(LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59)))
        .build();

    assertThatThrownBy(() -> facilitySchedules.addFacilitySchedule(createFacilitySchedule))
        .isInstanceOf(ConflictingTimeException.class);
  }

  @DisplayName("시설 스케줄을 업데이트 한다.")
  @Test
  public void updateFacilitySchedule() {
    FacilitySchedule updateFacilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(nowDate, LocalTime.of(0, 0, 0)))
        .endDateTime(LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59)))
        .build();
    List<FacilitySchedule> equalsLocalDate = facilitySchedules
        .isEqualsLocalDate(updateFacilitySchedule);
    FacilitySchedule targetSchedule = equalsLocalDate.get(0);

    facilitySchedules.updateFacilitySchedule(updateFacilitySchedule, targetSchedule);

    List<FacilitySchedule> retFacilitySchedules = facilitySchedules
        .isEqualsLocalDate(updateFacilitySchedule);
    assertAll(
        () -> retFacilitySchedules.get(0).isStartTimeEquals(LocalTime.of(0, 0, 0)),
        () -> retFacilitySchedules.get(0).isEndTimeEquals(LocalTime.of(23, 59, 59))
    );
  }

  @DisplayName("시설 스케줄을 업데이트 한다.하지만 범위가 연속적이므로 합쳐서 저장된다.")
  @Test
  public void updateFacilitySchedule_Merge() {
    FacilitySchedule createFacilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(nowDate, LocalTime.of(19, 0, 0)))
        .endDateTime(LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59)))
        .build();
    facilitySchedules.addFacilitySchedule(createFacilitySchedule);
    FacilitySchedule updateFacilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(nowDate, LocalTime.of(18, 0, 0)))
        .endDateTime(LocalDateTime.of(nowDate, LocalTime.of(22, 59, 59)))
        .build();
    facilitySchedules.updateFacilitySchedule(updateFacilitySchedule, createFacilitySchedule);

    List<FacilitySchedule> retFacilitySchedules = facilitySchedules
        .isEqualsLocalDate(updateFacilitySchedule);
    assertAll(
        () -> retFacilitySchedules.get(0).isStartTimeEquals(LocalTime.of(0, 0, 0)),
        () -> retFacilitySchedules.get(0).isEndTimeEquals(LocalTime.of(22, 59, 59))
    );
  }

  @DisplayName("시설 스케줄을 업데이트 시 기존 스케줄과 겹친다면 예외를 던진다.")
  @Test
  public void updateFacilitySchedule_throwException_ifConflict() {
    FacilitySchedule createFacilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(nowDate, LocalTime.of(19, 0, 0)))
        .endDateTime(LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59)))
        .build();
    facilitySchedules.addFacilitySchedule(createFacilitySchedule);
    FacilitySchedule updateFacilitySchedule = FacilitySchedule.builder()
        .startDateTime(LocalDateTime.of(nowDate, LocalTime.of(16, 0, 0)))
        .endDateTime(LocalDateTime.of(nowDate, LocalTime.of(22, 59, 59)))
        .build();

    assertThatThrownBy(() -> facilitySchedules
        .updateFacilitySchedule(updateFacilitySchedule, createFacilitySchedule))
        .isInstanceOf(ConflictingTimeException.class);
  }
}
