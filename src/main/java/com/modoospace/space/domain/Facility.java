package com.modoospace.space.domain;

import static javax.persistence.FetchType.LAZY;

import com.modoospace.common.BaseTimeEntity;
import com.modoospace.member.domain.Member;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
public class Facility extends BaseTimeEntity {

  @Id
  @GeneratedValue
  @Column(name = "facility_id")
  private Long id;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FacilityType facilityType;

  @Column(nullable = false)
  private Boolean reservationEnable;

  private String description;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "space_id")
  private Space space;

  @Embedded
  private TimeSettings timeSettings;

  @Embedded
  private WeekdaySettings weekdaySettings;

  @Embedded
  private FacilitySchedules facilitySchedules;

  @Builder
  public Facility(Long id, String name, FacilityType facilityType,
      Boolean reservationEnable, String description, Space space,
      TimeSettings timeSettings, WeekdaySettings weekdaySettings,
      FacilitySchedules facilitySchedules) {
    this.id = id;
    this.name = name;
    this.facilityType = facilityType;
    this.reservationEnable = reservationEnable;
    this.description = description;
    this.space = space;

    this.timeSettings = timeSettings;
    timeSettings.setFacility(this);

    this.weekdaySettings = weekdaySettings;
    weekdaySettings.setFacility(this);

    this.facilitySchedules = facilitySchedules;
    if (facilitySchedules == null) {
      this.facilitySchedules = FacilitySchedules
          .createFacilitySchedules(timeSettings, weekdaySettings);
    }
  }

  public void update(Facility facility, Member loginMember) {
    verifyManagementPermission(loginMember);

    this.name = facility.getName();
    this.reservationEnable = facility.getReservationEnable();
    this.description = facility.getDescription();

    if (!facility.getTimeSettings().isEmpty()) {
      this.timeSettings.update(facility.getTimeSettings(), this);
    }

    if (!facility.getWeekdaySettings().isEmpty()) {
      this.weekdaySettings.update(facility.getWeekdaySettings(), this);
    }

    if (!facility.getTimeSettings().isEmpty() || !facility.getWeekdaySettings().isEmpty()) {
      FacilitySchedules facilitySchedules = FacilitySchedules
          .createFacilitySchedules(this.timeSettings, this.weekdaySettings);
      this.facilitySchedules.update(facilitySchedules, this);
    }
  }

  public void updateSchedules(Facility facility, Member loginMember) {
    verifyManagementPermission(loginMember);

    this.facilitySchedules.update(facility.getFacilitySchedules(), this);
  }

  public void verifyManagementPermission(Member loginMember) {
    space.verifyManagementPermission(loginMember);
  }

  public boolean isOpen(LocalDateTime start, LocalDateTime end) {
    return facilitySchedules.isOpen(start, end);
  }
}
