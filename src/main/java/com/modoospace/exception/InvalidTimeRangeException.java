package com.modoospace.exception;

import java.time.LocalTime;

public class InvalidTimeRangeException extends RuntimeException {

  public InvalidTimeRangeException(LocalTime startTime, LocalTime endTime) {
    super("시작시간(" + startTime.toString() + ") 은 종료시간(" + endTime.toString() + ") 보다 이후일 수 없습니다.");
  }
}
