package com.modoospace.space.domain;

import com.modoospace.exception.InvalidTimeRangeException;
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

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public boolean isIncludedTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    return !this.endDateTime.isBefore(startDateTime) && !this.startDateTime.isAfter(endDateTime);
  }

  public boolean isStartTimeBeforeOrEquals(LocalDateTime startDateTime) {
    return this.startDateTime.isBefore(startDateTime) || this.startDateTime.isEqual(startDateTime);
  }

  public boolean isEndTimeAfterOrEquals(LocalDateTime endDateTime) {
    return this.endDateTime.isAfter(endDateTime) || this.endDateTime.isEqual(endDateTime);
  }

  public boolean is24TimeRange() {
    return this.startDateTime.toLocalTime().equals(LocalTime.of(0, 0, 0))
        && this.endDateTime.toLocalTime().equals(LocalTime.of(23, 59, 59));
  }

  public void update(FacilitySchedule facilitySchedule){
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
