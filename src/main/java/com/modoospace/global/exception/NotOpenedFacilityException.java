package com.modoospace.global.exception;

public class NotOpenedFacilityException extends RuntimeException {

  private static final String MESSAGE = "시설 이용 가능시간이 아닙니다.";

  public NotOpenedFacilityException() {
    super(MESSAGE);
  }
}
