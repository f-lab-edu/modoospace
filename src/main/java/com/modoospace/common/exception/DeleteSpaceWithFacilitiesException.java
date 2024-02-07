package com.modoospace.common.exception;

public class DeleteSpaceWithFacilitiesException extends RuntimeException {

    private static final String message = "시설을 갖는 공간은 삭제할 수 없습니다.";

    public DeleteSpaceWithFacilitiesException() {
        super(message);
    }
}
