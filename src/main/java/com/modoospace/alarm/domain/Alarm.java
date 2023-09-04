package com.modoospace.alarm.domain;

import com.modoospace.common.BaseTimeEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alarm extends BaseTimeEntity {

  @Id
  @GeneratedValue
  @Column(name = "alarm_id")
  private Long id;

  private Long memberId;

  private Long reservationId;

  private String facilityName;

  @Enumerated(EnumType.STRING)
  private AlarmType alarmType;

  @Builder
  public Alarm(Long id, Long memberId, Long reservationId, String facilityName,
      AlarmType alarmType) {
    this.id = id;
    this.memberId = memberId;
    this.reservationId = reservationId;
    this.facilityName = facilityName;
    this.alarmType = alarmType;
  }

  public String getAlarmMessage() {
    return facilityName + alarmType.getAlarmText();
  }
}
