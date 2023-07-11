package com.modoospace.space.domain;

import com.modoospace.exception.ConflictingTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
          .allMatch(facilitySchedule -> facilitySchedule
              .isIncludingTimeRange(startDateTime, endDateTime)); // 시간 범위를 전부 포함하고있는지 체크
    }

    // 다른 날짜의 시간을 체크하는 경우
    // 첫번째 스케줄 시작시간, 종료시간(23:59:59) 체크
    FacilitySchedule startDaySchedule = facilitySchedules.get(0);
    LocalDateTime startDayEndTime = LocalDateTime
        .of(startDateTime.getYear(), startDateTime.getMonthValue(), startDateTime.getDayOfMonth(),
            23, 59, 59);
    if (!startDaySchedule.isIncludingTimeRange(startDateTime, startDayEndTime)) {
      return false;
    }

    // 중간 스케줄 24시간 여부 체크
    if (!IntStream.range(1, facilitySchedules.size() - 1)
        .mapToObj(i -> facilitySchedules.get(i))
        .allMatch(FacilitySchedule::is24TimeRange)) {
      return false;
    }

    // 마지막 스케줄 시작시간(00:00:00), 종료시간 체크
    FacilitySchedule endDaySchedule = facilitySchedules.get(facilitySchedules.size() - 1);
    LocalDateTime endDayStartTime = LocalDateTime
        .of(endDateTime.getYear(), endDateTime.getMonthValue(), endDateTime.getDayOfMonth(),
            0, 0, 0);
    if (!endDaySchedule.isIncludingTimeRange(endDayStartTime, endDateTime)) {
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

  public void addFacilitySchedule(FacilitySchedule createSchedule) {
    // 1. create하는 날짜에 해당하는 스케줄 데이터 필터링 (쿼리로 가져올지 고민해보기)
    List<FacilitySchedule> oneDayFacilitySchedules = isEqualsLocalDate(createSchedule);

    // 2. create하려는 스케줄과 conflict 되는지 검증
    verifyConflict(createSchedule, oneDayFacilitySchedules);

    // 3. 스케줄 추가
    oneDayFacilitySchedules.add(createSchedule);

    // 4. 해당 날짜에서 시간범위가 연속적인 Schedule끼리 합쳐서 저장하기
    mergeAndUpdateSchedules(oneDayFacilitySchedules);
  }

  public void updateFacilitySchedule(FacilitySchedule updateSchedule, FacilitySchedule schedule) {
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
    mergeAndUpdateSchedules(oneDayFacilitySchedules);
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

  private void mergeAndUpdateSchedules(List<FacilitySchedule> facilitySchedules) {
    facilitySchedules
        .forEach(schedule -> this.facilitySchedules.remove(schedule));
    mergeSchedules(facilitySchedules);
    this.facilitySchedules.addAll(facilitySchedules);
  }

  private void mergeSchedules(List<FacilitySchedule> facilitySchedules) {
    boolean mergeOccurred = true;
    while (mergeOccurred) {
      mergeOccurred = false;
      Collections.sort(facilitySchedules, Comparator.comparing(FacilitySchedule::getStartDateTime));
      for (int i = 0; i < facilitySchedules.size() - 1; i++) {
        FacilitySchedule facilitySchedule1 = facilitySchedules.get(i);
        FacilitySchedule facilitySchedule2 = facilitySchedules.get(i + 1);
        FacilitySchedule mergeFacilitySchedule = FacilitySchedule
            .mergeFacilitySchedule(facilitySchedule1, facilitySchedule2);
        if (mergeFacilitySchedule != null) {
          facilitySchedules.remove(facilitySchedule1);
          facilitySchedules.remove(facilitySchedule2);
          facilitySchedules.add(mergeFacilitySchedule);
          mergeOccurred = true;
          break;
        }
      }
    }
  }

  @Override
  public String toString() {
    return "FacilitySchedules{" +
        "facilitySchedules=" + facilitySchedules +
        '}';
  }
}
