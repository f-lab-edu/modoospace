package com.modoospace.common.exception;

public class LimitNumOfUserException extends RuntimeException {

    private static final String MESSAGE = "시설 허용 인원에 미달 또는 초과입니다.";

    public LimitNumOfUserException() {
        super(MESSAGE);
    }
}
