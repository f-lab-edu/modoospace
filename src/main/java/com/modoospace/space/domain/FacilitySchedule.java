package com.modoospace.space.domain;

import com.modoospace.exception.InvalidTimeRangeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FacilitySchedule {

  @Id
  @GeneratedValue
  @Column(name = "facility_schedule_id")
  private Long id;

  @Column(nullable = false)
  private LocalDateTime startDateTime;

  @Column(nullable = false)
  private LocalDateTime endDateTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facility_id")
  private Facility facility;

  @Builder
  public FacilitySchedule(Long id, LocalDateTime startDateTime, LocalDateTime endDateTime,
      Facility facility) {
    this.id = id;

    validateDateTimeRange(startDateTime, endDateTime);
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;

    this.facility = facility;
  }

  private void validateDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    if (startDateTime.isAfter(endDateTime)) {
      throw new InvalidTimeRangeException(startDateTime, endDateTime);
    }
  }

  public static FacilitySchedule mergeFacilitySchedule(FacilitySchedule facilitySchedule1,
      FacilitySchedule facilitySchedule2) {
    if (facilitySchedule1
        .isStartDateTimeEquals(facilitySchedule2.getEndDateTime().plusSeconds(1))) {
      return FacilitySchedule.builder()
          .startDateTime(facilitySchedule2.getStartDateTime())
          .endDateTime(facilitySchedule1.getEndDateTime())
          .facility(facilitySchedule1.getFacility())
          .build();
    }

    if (facilitySchedule1
        .isEndDateTimeEquals(facilitySchedule2.getStartDateTime().minusSeconds(1))) {
      return FacilitySchedule.builder()
          .startDateTime(facilitySchedule1.getStartDateTime())
          .endDateTime(facilitySchedule2.getEndDateTime())
          .facility(facilitySchedule1.getFacility())
          .build();
    }

    return null;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public boolean isIncludedTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    return !this.endDateTime.isBefore(startDateTime) && !this.startDateTime.isAfter(endDateTime);
  }

  public boolean isIncludingTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    return (this.startDateTime.isBefore(startDateTime) || isStartDateTimeEquals(startDateTime))
        && (this.endDateTime.isAfter(endDateTime) || isEndDateTimeEquals(endDateTime));
  }

  public boolean isStartEndDateEquals(FacilitySchedule facilitySchedule) {
    LocalDate startDate = facilitySchedule.getStartDateTime().toLocalDate();
    LocalDate endDate = facilitySchedule.getEndDateTime().toLocalDate();
    return isStartEndDateEquals(startDate, endDate);
  }

  public boolean isStartEndDateEquals(LocalDate startDate, LocalDate endDate) {
    return isStartDateEquals(startDate) && isEndDateEquals(endDate);
  }

  public boolean isStartDateEquals(LocalDate startDate) {
    return this.startDateTime.toLocalDate().equals(startDate);
  }

  public boolean isStartTimeEquals(LocalTime startTime) {
    return this.startDateTime.toLocalTime().equals(startTime);
  }

  public boolean isStartDateTimeEquals(LocalDateTime startDateTime) {
    return this.startDateTime.isEqual(startDateTime);
  }

  public boolean isEndDateEquals(LocalDate endDate) {
    return this.endDateTime.toLocalDate().equals(endDate);
  }

  public boolean isEndTimeEquals(LocalTime endTime) {
    return this.endDateTime.toLocalTime().equals(endTime);
  }

  public boolean isEndDateTimeEquals(LocalDateTime endDateTime) {
    return this.endDateTime.isEqual(endDateTime);
  }

  public boolean is24TimeRange() {
    return isStartTimeEquals(LocalTime.of(0, 0, 0))
        && isEndTimeEquals(LocalTime.of(23, 59, 59));
  }

  public void update(FacilitySchedule facilitySchedule) {
    startDateTime = facilitySchedule.getStartDateTime();
    endDateTime = facilitySchedule.getEndDateTime();
  }

  @Override
  public String toString() {
    return "FacilitySchedule{" +
        "startDateTime=" + startDateTime +
        ", endDateTime=" + endDateTime +
        '}';
  }
}
