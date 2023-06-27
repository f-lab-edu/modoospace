package com.modoospace.space.domain;

import com.modoospace.exception.InvalidTimeRangeException;
import java.time.LocalDateTime;
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

  @Override
  public String toString() {
    return "FacilitySchedule{" +
        "startDateTime=" + startDateTime +
        ", endDateTime=" + endDateTime +
        '}';
  }
}
