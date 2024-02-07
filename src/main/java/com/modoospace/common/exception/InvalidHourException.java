package com.modoospace.common.exception;

public class InvalidHourException extends RuntimeException {

    public InvalidHourException(Integer hour) {
        super("올바른 시간이 아닙니다.(" + hour + ")");
    }
}
