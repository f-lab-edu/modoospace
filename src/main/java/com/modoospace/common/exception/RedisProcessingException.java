package com.modoospace.common.exception;

public class RedisProcessingException extends RuntimeException {

  private static final String MESSAGE = "Redis 처리 중 문제가 생겼습니다.";

  public RedisProcessingException() {
    super(MESSAGE);
  }
}
