package com.modoospace.space.domain;

import com.modoospace.exception.DuplicatedWeekdayException;
import java.time.DayOfWeek;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
public class WeekdaySetting {

  @Id
  @GeneratedValue
  @Column(name = "weekday_setting_id")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DayOfWeek weekday;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facility_id")
  private Facility facility;

  @Builder
  public WeekdaySetting(Long id, DayOfWeek weekday, Facility facility) {
    this.id = id;
    this.weekday = weekday;
    this.facility = facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public void isDuplicatedWeekday(WeekdaySetting compareWeekdaySetting) {
    if (weekday.equals(compareWeekdaySetting.getWeekday())) {
      throw new DuplicatedWeekdayException(weekday);
    }
  }

  public boolean isEqualWeekday(DayOfWeek weekday) {
    return this.weekday.equals(weekday);
  }

  @Override
  public String toString() {
    return "WeekdaySetting{" +
        "weekday=" + weekday +
        '}';
  }
}
