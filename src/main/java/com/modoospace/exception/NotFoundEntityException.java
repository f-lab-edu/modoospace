package com.modoospace.exception;

public class NotFoundEntityException extends RuntimeException{

  private static final String MSG = "해당 데이터를 찾을 수 없습니다.";

  public NotFoundEntityException() {
    super(MSG);
  }

  public NotFoundEntityException(String message) {
    super(message);
  }
}
