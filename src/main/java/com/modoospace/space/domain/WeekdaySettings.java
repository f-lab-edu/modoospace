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

  // TODO : 프록시 공부 후 수정 필요
  @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<WeekdaySetting> weekdaySettings = new ArrayList<>();

  public WeekdaySettings(List<WeekdaySetting> weekdaySettings) {
    validateWeekdaySettings(weekdaySettings);
    this.weekdaySettings = weekdaySettings;
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

  public void setFacility(Facility facility) {
    for (WeekdaySetting weekdaySetting : weekdaySettings) {
      weekdaySetting.setFacility(facility);
    }
  }

  public boolean isContainWeekday(DayOfWeek dayOfWeek) {
    return weekdaySettings.stream()
        .anyMatch(weekdaySetting -> weekdaySetting.isEqualWeekday(dayOfWeek));
  }
}
