package com.modoospace.exception;

public class NotOpenedFacilityException extends RuntimeException {

  private static final String MESSAGE = "권한이 없습니다.";

  public NotOpenedFacilityException() {
    super(MESSAGE);
  }
}
