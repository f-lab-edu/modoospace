package com.modoospace.space.domain;

import com.modoospace.global.exception.ConflictingTimeException;
import com.modoospace.global.exception.InvalidTimeRangeException;
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
public class TimeSetting {

  @Id
  @GeneratedValue
  @Column(name = "time_setting_id")
  private Long id;

  @Column(nullable = false)
  private LocalTime startTime;

  @Column(nullable = false)
  private LocalTime endTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facility_id")
  private Facility facility;

  @Builder
  public TimeSetting(Long id, LocalTime startTime, LocalTime endTime,
      Facility facility) {
    this.id = id;

    validateTimeRange(startTime, endTime);
    this.startTime = startTime;
    this.endTime = endTime;

    this.facility = facility;
  }

  private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
    if (startTime.isAfter(endTime)) {
      throw new InvalidTimeRangeException(startTime, endTime);
    }
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public void verifyConflicting(TimeSetting compareTimeSetting) {
    if (!endTime.isBefore(compareTimeSetting.getStartTime()) &&
        !compareTimeSetting.getEndTime().isBefore(startTime)) {
      throw new ConflictingTimeException(this, compareTimeSetting);
    }
  }

  public FacilitySchedule createFacilitySchedule(LocalDate scheduleDate) {
    LocalDateTime startDateTime = LocalDateTime.of(scheduleDate, this.startTime);
    LocalDateTime endDateTime = LocalDateTime.of(scheduleDate, this.endTime);

    return FacilitySchedule.builder()
        .startDateTime(startDateTime)
        .endDateTime(endDateTime)
        .facility(this.facility)
        .build();
  }

  @Override
  public String toString() {
    return "TimeSetting{" +
        "startTime=" + startTime.toString() +
        ", endTime=" + endTime.toString() +
        '}';
  }
}
