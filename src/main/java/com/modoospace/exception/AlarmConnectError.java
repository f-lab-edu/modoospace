package com.modoospace.exception;

public class AlarmConnectError extends RuntimeException {

  private static final String MESSAGE = "알람 연결에 문제가 생겼습니다.";

  public AlarmConnectError() {
    super(MESSAGE);
  }
}
