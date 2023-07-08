package com.modoospace.space.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
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
public class TimeSettings {

  @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<TimeSetting> timeSettings = new ArrayList<>();

  public TimeSettings(List<TimeSetting> timeSettings) {
    validateTimeSettings(timeSettings);
    this.timeSettings = timeSettings;
  }

  private void validateTimeSettings(List<TimeSetting> timeSettings) {
    Collections.sort(timeSettings, Comparator.comparing(TimeSetting::getStartTime));

    for (int i = 0; i < timeSettings.size() - 1; i++) {
      TimeSetting timeSetting = timeSettings.get(i);
      TimeSetting compareTimeSetting = timeSettings.get(i + 1);
      timeSetting.verifyConflicting(compareTimeSetting);
    }
  }

  public void setFacility(Facility facility) {
    for (TimeSetting timeSetting : timeSettings) {
      timeSetting.setFacility(facility);
    }
  }

  public List<FacilitySchedule> createFacilitySchedules(LocalDate scheduleDate) {
    return timeSettings.stream()
        .map(timeSetting -> timeSetting.createFacilitySchedule(scheduleDate))
        .collect(Collectors.toList());
  }

  public boolean isEmpty() {
    return timeSettings.isEmpty();
  }

  public void update(TimeSettings timeSettings, Facility facility) {
    this.timeSettings.clear();
    this.timeSettings.addAll(timeSettings.getTimeSettings());
    timeSettings.setFacility(facility);
  }
}
