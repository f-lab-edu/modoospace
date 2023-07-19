package com.modoospace.exception;

public class AlarmSendException extends RuntimeException{

  private static final String MESSAGE = "알람 발신에 문제가 생겼습니다.";

  public AlarmSendException() {
    super(MESSAGE);
  }
}
