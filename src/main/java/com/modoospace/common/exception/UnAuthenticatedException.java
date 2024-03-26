package com.modoospace.common.exception;

public class UnAuthenticatedException extends RuntimeException {

    private static final String MESSAGE = "로그인이 필요합니다.";

    public UnAuthenticatedException() {
        super(MESSAGE);
    }
}
