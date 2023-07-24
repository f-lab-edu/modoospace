package com.modoospace.space.domain;

import com.modoospace.global.exception.ConflictingTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

  public static FacilitySchedules create3MonthFacilitySchedules(TimeSettings timeSettings,
      WeekdaySettings weekdaySettings, YearMonth createYearMonth) {
    LocalDate startDate = createYearMonth.atDay(1);
    int daysBetween = startDate.lengthOfMonth()
        + startDate.plusMonths(1).lengthOfMonth()
        + startDate.plusMonths(2).lengthOfMonth();

    return createFacilitySchedules(timeSettings, weekdaySettings, startDate, daysBetween);
  }

  public static FacilitySchedules create1MonthFacilitySchedules(TimeSettings timeSettings,
      WeekdaySettings weekdaySettings, YearMonth createYearMonth) {
    LocalDate startDate = createYearMonth.atDay(1);
    int daysBetween = startDate.lengthOfMonth();

    return createFacilitySchedules(timeSettings, weekdaySettings, startDate, daysBetween);
  }

  private static FacilitySchedules createFacilitySchedules(TimeSettings timeSettings,
      WeekdaySettings weekdaySettings, LocalDate startDate, int daysBetween) {
    List<FacilitySchedule> facilitySchedules = new ArrayList<>();
    IntStream.range(0, daysBetween)
        .mapToObj(startDate::plusDays)
        .filter(scheduleDate -> weekdaySettings.isContainWeekday(scheduleDate.getDayOfWeek()))
        .flatMap(scheduleDate -> timeSettings.createFacilitySchedules(scheduleDate).stream())
        .forEach(facilitySchedules::add);

    return new FacilitySchedules(facilitySchedules);
  }

  public void update(FacilitySchedules facilitySchedules, Facility facility) {
    this.facilitySchedules.clear();
    this.facilitySchedules.addAll(facilitySchedules.getFacilitySchedules());
    facilitySchedules.setFacility(facility);
  }

  public void addAll(FacilitySchedules facilitySchedules, Facility facility) {
    this.facilitySchedules.addAll(facilitySchedules.getFacilitySchedules());
    facilitySchedules.setFacility(facility);
  }

  private void setFacility(Facility facility) {
    for (FacilitySchedule facilitySchedule : facilitySchedules) {
      facilitySchedule.setFacility(facility);
    }
  }

  public FacilitySchedule addFacilitySchedule(FacilitySchedule createSchedule) {
    // 1. create하는 날짜에 해당하는 스케줄 데이터 필터링 (쿼리로 가져올지 고민해보기)
    List<FacilitySchedule> oneDayFacilitySchedules = isEqualsLocalDate(createSchedule);

    // 2. create하려는 스케줄과 conflict 되는지 검증
    verifyConflict(createSchedule, oneDayFacilitySchedules);

    // 3. 스케줄 추가
    oneDayFacilitySchedules.add(createSchedule);

    // 4. 해당 날짜에서 시간범위가 연속적인 Schedule끼리 합쳐서 저장하기
    Optional<FacilitySchedule> mergedSchedule = mergeAndUpdateSchedules(oneDayFacilitySchedules);

    return mergedSchedule.orElse(createSchedule);
  }

  public FacilitySchedule updateFacilitySchedule(FacilitySchedule updateSchedule,
      FacilitySchedule schedule) {
    // 1. update 하는 날짜에 해당하는 스케줄 데이터 필터링 (쿼리로 가져올지 고민해보기)
    List<FacilitySchedule> oneDayFacilitySchedules = isEqualsLocalDate(updateSchedule);

    // 2. update 하려는 스케줄과 conflict 되는지 검증
    List<FacilitySchedule> targetExceptedSchedules = oneDayFacilitySchedules.stream()
        .filter(facilitySchedule -> facilitySchedule != schedule)
        .collect(Collectors.toList());
    verifyConflict(updateSchedule, targetExceptedSchedules);

    // 3. 스케줄 업데이트
    schedule.update(updateSchedule);

    // 4. 해당 날짜에서 시간범위가 연속적인 Schedule끼리 합쳐서 저장하기
    Optional<FacilitySchedule> mergedSchedule = mergeAndUpdateSchedules(oneDayFacilitySchedules);

    return mergedSchedule.orElse(schedule);
  }

  public List<FacilitySchedule> isEqualsLocalDate(FacilitySchedule targetFacilitySchedule) {
    LocalDate startDate = targetFacilitySchedule.getStartDateTime().toLocalDate();
    LocalDate endDate = targetFacilitySchedule.getEndDateTime().toLocalDate();
    return isEqualsLocalDate(startDate, endDate);
  }

  public List<FacilitySchedule> isEqualsLocalDate(LocalDate startDate, LocalDate endDate) {
    return this.facilitySchedules.stream()
        .filter(facilitySchedule -> facilitySchedule.isStartEndDateEquals(startDate, endDate))
        .collect(Collectors.toList());
  }

  private void verifyConflict(FacilitySchedule targetSchedule,
      List<FacilitySchedule> oneDayFacilitySchedules) {
    Optional<FacilitySchedule> conflictSchedule = oneDayFacilitySchedules.stream()
        .filter(facilitySchedule -> facilitySchedule.isIncludedTimeRange(
            targetSchedule.getStartDateTime(), targetSchedule.getEndDateTime()
        )).findAny();
    if (conflictSchedule.isPresent()) {
      throw new ConflictingTimeException(targetSchedule, conflictSchedule.get());
    }
  }

  private Optional<FacilitySchedule> mergeAndUpdateSchedules(
      List<FacilitySchedule> facilitySchedules) {
    this.facilitySchedules.removeAll(facilitySchedules);
    Optional<FacilitySchedule> mergedSchedule = mergeAllSchedules(facilitySchedules);
    this.facilitySchedules.addAll(facilitySchedules);
    return mergedSchedule;
  }

  private Optional<FacilitySchedule> mergeAllSchedules(List<FacilitySchedule> facilitySchedules) {
    Optional<FacilitySchedule> retMergedSchedule = Optional.empty();
    while (true) {
      Collections.sort(facilitySchedules, Comparator.comparing(FacilitySchedule::getStartDateTime));
      Optional<FacilitySchedule> mergedSchedule = mergeSchedules(facilitySchedules);
      if (mergedSchedule.isEmpty()) {
        break;
      }
      retMergedSchedule = mergedSchedule;
    }
    return retMergedSchedule;
  }

  private Optional<FacilitySchedule> mergeSchedules(List<FacilitySchedule> facilitySchedules) {
    for (int i = 0; i < facilitySchedules.size() - 1; i++) {
      FacilitySchedule facilitySchedule1 = facilitySchedules.get(i);
      FacilitySchedule facilitySchedule2 = facilitySchedules.get(i + 1);
      Optional<FacilitySchedule> mergeFacilitySchedule = FacilitySchedule
          .mergeFacilitySchedule(facilitySchedule1, facilitySchedule2);

      if (mergeFacilitySchedule.isPresent()) {
        facilitySchedules.removeAll(List.of(facilitySchedule1, facilitySchedule2));
        facilitySchedules.add(mergeFacilitySchedule.get());
        return mergeFacilitySchedule;
      }
    }

    return Optional.empty();
  }

  @Override
  public String toString() {
    return "FacilitySchedules{" +
        "facilitySchedules=" + facilitySchedules +
        '}';
  }
}
