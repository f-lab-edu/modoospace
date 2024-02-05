package com.modoospace.common.exception;

public class InvalidNumOfUserException extends RuntimeException {

    public InvalidNumOfUserException(Integer numOfUser) {
        super("올바른 사용자 수가 아닙니다. (" + numOfUser + ")");
    }

    public InvalidNumOfUserException(Integer minUser, Integer maxUser) {
        super("최소사용자수는 최대사용자수 보다 클 수 없습니다. (minUser=" + minUser + ", maxUser=" + maxUser + ")");
    }

}
