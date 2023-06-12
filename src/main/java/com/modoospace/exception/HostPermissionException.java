package com.modoospace.exception;

public class HostPermissionException extends RuntimeException{

  private static final String MSG = "호스트 권한이 필요합니다.";

  public HostPermissionException() {
    super(MSG);
  }

  public HostPermissionException(String message) {
    super(message);
  }
}
