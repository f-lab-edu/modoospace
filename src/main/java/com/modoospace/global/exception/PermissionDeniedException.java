package com.modoospace.global.exception;

public class PermissionDeniedException extends RuntimeException{

  private static final String MESSAGE = "권한이 없습니다.";

  public PermissionDeniedException() {
    super(MESSAGE);
  }
}
