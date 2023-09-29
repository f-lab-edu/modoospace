package com.modoospace.common.exception;

public class AlarmSendException extends RuntimeException {

  private static final String MESSAGE = "알람 전송에 실패했습니다.";

  public AlarmSendException() {
    super(MESSAGE);
  }
}
