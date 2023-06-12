package com.modoospace.exception;

public class VisitorPermissionException extends RuntimeException {

  private static final String MSG = "방문자 권한이 필요합니다.";

  public VisitorPermissionException() {
    super(MSG);
  }
}
