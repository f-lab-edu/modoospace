package com.modoospace.exception;

import com.modoospace.space.domain.Weekday;

public class DuplicatedWeekdayException extends RuntimeException {

  public DuplicatedWeekdayException(Weekday weekday) {
    super("요일(" + weekday + ")이 중복됩니다.");
  }
}
