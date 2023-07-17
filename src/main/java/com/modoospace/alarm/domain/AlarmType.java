package com.modoospace.alarm.domain;

import lombok.Getter;

@Getter
public enum AlarmType {
  NEW_RESERVATION("new reservation!"),
  APPROVED_RESERVATION("approved reservation!");

  private String alarmText;

  private AlarmType(String alarmText) {
    this.alarmText = alarmText;
  }
}
