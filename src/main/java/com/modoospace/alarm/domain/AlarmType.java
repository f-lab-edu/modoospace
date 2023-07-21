package com.modoospace.alarm.domain;

import lombok.Getter;

@Getter
public enum AlarmType {
  NEW_RESERVATION("의 예약이 생성되었습니다."),
  APPROVED_RESERVATION("의 예약이 승인되었습니다.");

  private String alarmText;

  private AlarmType(String alarmText) {
    this.alarmText = alarmText;
  }
}
