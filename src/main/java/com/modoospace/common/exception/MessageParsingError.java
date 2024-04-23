package com.modoospace.common.exception;

public class MessageParsingError extends RuntimeException {

    private static final String MESSAGE = "메세지를 파싱할 수 없습니다.";
    public MessageParsingError() {
        super(MESSAGE);
    }

    public MessageParsingError(String message) {
        super(message);
    }
}
