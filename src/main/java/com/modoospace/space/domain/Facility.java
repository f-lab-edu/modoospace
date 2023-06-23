package com.modoospace.space.domain;

import static javax.persistence.FetchType.LAZY;

import com.modoospace.common.BaseTimeEntity;
import com.modoospace.exception.DuplicatedWeekdayException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
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

  @Builder.Default
  @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL)
  private List<TimeSetting> timeSettings = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL)
  private List<WeekdaySetting> weekdaySettings = new ArrayList<>();

  public Facility(Long id, String name, FacilityType facilityType,
      Boolean reservationEnable, String description, Space space,
      List<TimeSetting> timeSettings,
      List<WeekdaySetting> weekdaySettings) {
    this.id = id;
    this.name = name;
    this.facilityType = facilityType;
    this.reservationEnable = reservationEnable;
    this.description = description;
    this.space = space;

    validateTimeSettings(timeSettings);
    this.timeSettings = timeSettings;
    for (TimeSetting timeSetting : timeSettings) {
      timeSetting.setFacility(this);
    }

    validateWeekdaySettings(weekdaySettings);
    this.weekdaySettings = weekdaySettings;
    for (WeekdaySetting weekdaySetting : weekdaySettings) {
      weekdaySetting.setFacility(this);
    }

    // TODO : 오늘 날짜로 3개월 이후까지 스케줄 데이터 생성 필요
  }

  private void validateTimeSettings(List<TimeSetting> timeSettings) {
    Collections.sort(timeSettings, Comparator.comparing(TimeSetting::getStartTime));

    for (int i = 0; i < timeSettings.size() - 1; i++) {
      TimeSetting timeSetting = timeSettings.get(i);
      for (int j = i + 1; j < timeSettings.size(); j++) {
        TimeSetting compareTimeSetting = timeSettings.get(j);
        timeSetting.verifyConflicting(compareTimeSetting);
      }
    }
  }

  private void validateWeekdaySettings(List<WeekdaySetting> weekdaySettings) {
    Collections.sort(weekdaySettings, Comparator.comparing(WeekdaySetting::getWeekday));

    for (int i = 0; i < weekdaySettings.size() - 1; i++) {
      WeekdaySetting weekdaySetting = weekdaySettings.get(i);
      for (int j = i + 1; j < weekdaySettings.size(); j++) {
        WeekdaySetting compareWeekdaySetting = weekdaySettings.get(j);
        weekdaySetting.isDuplicatedWeekday(compareWeekdaySetting);
      }
    }
  }
}
