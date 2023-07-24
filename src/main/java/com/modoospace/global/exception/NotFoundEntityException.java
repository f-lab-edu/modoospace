package com.modoospace.global.exception;

public class NotFoundEntityException extends RuntimeException{

  public NotFoundEntityException(String entity) {
    super("해당 " + entity + "을(를) 찾을 수 없습니다.");
  }

  public NotFoundEntityException(String entity, Long identifier) {
    super("해당 " + entity + "("+identifier+") 을(를) 찾을 수 없습니다.");
  }

  public NotFoundEntityException(String entity, String identifier) {
    super("해당 " + entity + "("+identifier+") 을(를) 찾을 수 없습니다.");
  }
}
