package com.modoospace.space.domain;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
public class WeekdaySettings {

  @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<WeekdaySetting> weekdaySettings = new ArrayList<>();

  public WeekdaySettings(List<WeekdaySetting> weekdaySettings) {
    validateWeekdaySettings(weekdaySettings);
    this.weekdaySettings = weekdaySettings;
  }

  private void validateWeekdaySettings(List<WeekdaySetting> weekdaySettings) {
    Collections.sort(weekdaySettings, Comparator.comparing(WeekdaySetting::getWeekday));

    for (int i = 0; i < weekdaySettings.size() - 1; i++) {
      WeekdaySetting weekdaySetting = weekdaySettings.get(i);
      WeekdaySetting compareWeekdaySetting = weekdaySettings.get(i + 1);
      weekdaySetting.verifyDuplicated(compareWeekdaySetting);
    }
  }

  public void setFacility(Facility facility) {
    for (WeekdaySetting weekdaySetting : weekdaySettings) {
      weekdaySetting.setFacility(facility);
    }
  }

  public boolean isContainWeekday(DayOfWeek dayOfWeek) {
    return weekdaySettings.stream()
        .anyMatch(weekdaySetting -> weekdaySetting.isEqualWeekday(dayOfWeek));
  }

  public boolean isEmpty() {
    return weekdaySettings.isEmpty();
  }

  public void update(WeekdaySettings weekdaySettings, Facility facility) {
    this.weekdaySettings.clear();
    this.weekdaySettings.addAll(weekdaySettings.getWeekdaySettings());
    weekdaySettings.setFacility(facility);
  }
}
