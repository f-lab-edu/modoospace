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
    LocalDate nowMonthDate = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1);
    int daysBetween = (int) ChronoUnit.DAYS.between(nowMonthDate, nowMonthDate.plusMonths(3));

    List<FacilitySchedule> facilitySchedules = new ArrayList<>();
    IntStream.range(0, daysBetween)
        .mapToObj(nowMonthDate::plusDays)
        .filter(scheduleDate -> weekdaySettings.isContainWeekday(scheduleDate.getDayOfWeek()))
        .flatMap(scheduleDate -> timeSettings.createFacilitySchedules(scheduleDate).stream())
        .forEach(facilitySchedules::add);

    return new FacilitySchedules(facilitySchedules);
  }

  public void clear() {
    facilitySchedules.clear();
  }

  public void setFacility(Facility facility) {
    for (FacilitySchedule facilitySchedule : facilitySchedules) {
      facilitySchedule.setFacility(facility);
    }
  }

  public boolean isOpen(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    List<FacilitySchedule> facilitySchedules = this.facilitySchedules.stream()
        .filter(
            facilitySchedule -> facilitySchedule.isIncludedTimeRange(startDateTime, endDateTime))
        .collect(Collectors.toList());

    // 같은 날짜의 시간을 체크하는 경우
    if (startDateTime.toLocalDate().isEqual(endDateTime.toLocalDate())) {
      return facilitySchedules.stream()
          .allMatch(facilitySchedule -> facilitySchedule
              .isIncludingTimeRange(startDateTime, endDateTime)); // 시간 범위를 전부 포함하고있는지 체크
    }

    // 다른 날짜의 시간을 체크하는 경우
    return facilitySchedules.stream()
        .allMatch(FacilitySchedule::is24TimeRange); // 시간 범위가 24시간인지 체크
  }

  @Override
  public String toString() {
    return "FacilitySchedules{" +
        "facilitySchedules=" + facilitySchedules +
        '}';
  }

  public void update(FacilitySchedules facilitySchedules) {
    this.facilitySchedules.clear();
    this.facilitySchedules.addAll(facilitySchedules.getFacilitySchedules());
  }
}
