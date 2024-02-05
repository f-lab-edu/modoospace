package com.modoospace.common.exception;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class InvalidTimeRangeException extends RuntimeException {

    public InvalidTimeRangeException(LocalTime startHour, LocalTime endHour) {
        super("시작 시간(" + startHour + ") 은 마지막 시작 시간(" + endHour + ") 보다 이후일 수 없습니다.");
    }

    public InvalidTimeRangeException(LocalDateTime startDate, LocalDateTime endDate) {
        super("시작 시간(" + startDate.toString() + ") 은 마지막 시작 시간(" + endDate.toString()
            + ") 보다 이후일 수 없습니다.");
    }
}
