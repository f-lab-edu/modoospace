package com.modoospace.common.exception;

public class SSEConnectError extends RuntimeException {

  private static final String MESSAGE = "알람 연결에 문제가 생겼습니다.";

  public SSEConnectError() {
    super(MESSAGE);
  }
}
