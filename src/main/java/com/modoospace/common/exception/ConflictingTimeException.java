package com.modoospace.common.exception;

import com.modoospace.space.domain.Schedule;
import com.modoospace.space.domain.TimeSetting;

public class ConflictingTimeException extends RuntimeException {

  public ConflictingTimeException(TimeSetting timeSetting1, TimeSetting timeSetting2) {
    super(timeSetting1 + "과 " + timeSetting2 + " 시간이 겹칩니다.");
  }

  public ConflictingTimeException(Schedule schedule1, Schedule schedule2) {
    super(schedule1 + "과 " + schedule2 + " 시간이 겹칩니다.");
  }
}
