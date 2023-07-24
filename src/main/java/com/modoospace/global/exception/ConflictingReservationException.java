package com.modoospace.global.exception;

public class ConflictingReservationException extends RuntimeException {

  private static final String MESSAGE = "동일한 시간대에 예약이 존재합니다.";

  public ConflictingReservationException() {
    super(MESSAGE);
  }
}
