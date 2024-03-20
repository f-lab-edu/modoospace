package com.modoospace.common.exception;

public class EmptyResponseException extends RuntimeException {
    public EmptyResponseException(String response, String identifier) {
        super(response+"("+identifier+")응답 값이 비었습니다.");
    }
}
