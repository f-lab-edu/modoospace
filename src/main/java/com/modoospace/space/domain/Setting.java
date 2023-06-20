package com.modoospace.space.domain;

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
public class Setting {

  @Id
  @GeneratedValue
  @Column(name = "setting_id")
  private Long id;

  @Column(nullable = false)
  private LocalTime startTime;

  @Column(nullable = false)
  private LocalTime endTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facility_id")
  private Facility facility;

  @Builder
  public Setting(Long id, LocalTime startTime, LocalTime endTime,
      Facility facility) {
    this.id = id;
    this.startTime = startTime;
    this.endTime = endTime;
    this.facility = facility;
  }
}
