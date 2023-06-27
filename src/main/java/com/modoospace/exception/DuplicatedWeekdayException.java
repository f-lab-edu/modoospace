package com.modoospace.exception;

import java.time.DayOfWeek;

public class DuplicatedWeekdayException extends RuntimeException {

  public DuplicatedWeekdayException(DayOfWeek weekday) {
    super("요일(" + weekday + ")이 중복됩니다.");
  }
}
