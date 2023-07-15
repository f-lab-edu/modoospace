package com.modoospace.space.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FacilitySchedules {

  @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<FacilitySchedule> facilitySchedules = new ArrayList<>();

  public FacilitySchedules(List<FacilitySchedule> facilitySchedules) {
    this.facilitySchedules = facilitySchedules;
  }

  public static FacilitySchedules createFacilitySchedules(TimeSettings timeSettings,
      WeekdaySettings weekdaySettings) {
    LocalDate nowMonthStartDate = LocalDate
        .of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1);
    int daysBetween = (int) ChronoUnit.DAYS
        .between(nowMonthStartDate, nowMonthStartDate.plusMonths(3));

    List<FacilitySchedule> facilitySchedules = new ArrayList<>();
    IntStream.range(0, daysBetween)
        .mapToObj(nowMonthStartDate::plusDays)
        .filter(scheduleDate -> weekdaySettings.isContainWeekday(scheduleDate.getDayOfWeek()))
        .flatMap(scheduleDate -> timeSettings.createFacilitySchedules(scheduleDate).stream())
        .forEach(facilitySchedules::add);

    return new FacilitySchedules(facilitySchedules);
  }

  public boolean isOpen(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    List<FacilitySchedule> facilitySchedules = this.facilitySchedules.stream()
        .filter(
            facilitySchedule -> facilitySchedule.isIncludedTimeRange(startDateTime, endDateTime))
        .collect(Collectors.toList());

    if (facilitySchedules.isEmpty()) {
      return false;
    }

    // 같은 날짜의 시간을 체크하는 경우
    if (startDateTime.toLocalDate().isEqual(endDateTime.toLocalDate())) {
      return facilitySchedules.stream()
          .allMatch(facilitySchedule -> facilitySchedule.isEndTimeAfterOrEquals(startDateTime)
              && facilitySchedule.isEndTimeAfterOrEquals(endDateTime)); // 시간 범위를 전부 포함하고있는지 체크
    }

    // 다른 날짜의 시간을 체크하는 경우
    // 첫번째 스케줄 시작시간, 종료시간(23:59:59) 체크
    FacilitySchedule startDaySchedule = facilitySchedules.get(0);
    LocalDateTime startDateEndTime = LocalDateTime
        .of(startDateTime.getYear(), startDateTime.getMonthValue(), startDateTime.getDayOfMonth(),
            23, 59, 59);
    if (!startDaySchedule.isStartTimeBeforeOrEquals(startDateTime) || !startDaySchedule
        .isEndTimeAfterOrEquals(startDateEndTime)) {
      return false;
    }

    // 중간 스케줄 24시간 여부 체크
    if (!IntStream.range(1, facilitySchedules.size() - 1)
        .mapToObj(facilitySchedules::get)
        .allMatch(FacilitySchedule::is24TimeRange)) {
      return false;
    }

    // 마지막 스케줄 시작시간(0:0:0), 종료시간 체크
    FacilitySchedule endDaySchedule = facilitySchedules.get(facilitySchedules.size() - 1);
    LocalDateTime endDateStartTime = LocalDateTime
        .of(endDateTime.getYear(), endDateTime.getMonthValue(), endDateTime.getDayOfMonth(),
            0, 0, 0);
    if (!endDaySchedule.isStartTimeBeforeOrEquals(endDateStartTime) || !endDaySchedule
        .isEndTimeAfterOrEquals(endDateTime)) {
      return false;
    }

    return true;
  }

  public void update(FacilitySchedules facilitySchedules, Facility facility) {
    this.facilitySchedules.clear();
    this.facilitySchedules.addAll(facilitySchedules.getFacilitySchedules());
    facilitySchedules.setFacility(facility);
  }

  private void setFacility(Facility facility) {
    for (FacilitySchedule facilitySchedule : facilitySchedules) {
      facilitySchedule.setFacility(facility);
    }
  }

  @Override
  public String toString() {
    return "FacilitySchedules{" +
        "facilitySchedules=" + facilitySchedules +
        '}';
  }
}
