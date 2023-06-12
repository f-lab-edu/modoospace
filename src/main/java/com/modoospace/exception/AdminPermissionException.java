package com.modoospace.exception;

public class AdminPermissionException extends RuntimeException {

  private static final String MSG = "관리자 권한이 필요합니다.";

  public AdminPermissionException() {
    super(MSG);
  }
}
