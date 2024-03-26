package com.modoospace.common;

import com.modoospace.common.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlarmSendException.class)
    public ResponseEntity<String> handleAlarmSendException(InvalidTimeRangeException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SSEConnectError.class)
    public ResponseEntity<String> handleSSEConnectError(SSEConnectError exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RedisProcessingException.class)
    public ResponseEntity<String> handleRedisProcessingException(SSEConnectError exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConflictingReservationException.class)
    public ResponseEntity<String> handleConflictingReservationException(ConflictingReservationException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ConflictingTimeException.class)
    public ResponseEntity<String> handleConflictingTimeException(ConflictingTimeException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DuplicatedWeekdayException.class)
    public ResponseEntity<String> handleDuplicatedWeekdayException(ConflictingTimeException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<String> handlePermissionDeniedException(PermissionDeniedException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnAuthenticatedException.class)
    public ResponseEntity<String> handleUnAuthenticatedException(UnAuthenticatedException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotFoundEntityException.class)
    public ResponseEntity<String> handleNotFoundEntityException(NotFoundEntityException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmptyResponseException.class)
    public ResponseEntity<String> handleEmptyResponseException(EmptyResponseException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidTimeRangeException.class)
    public ResponseEntity<String> handleInvalidTimeRangeException(InvalidTimeRangeException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotOpenedFacilityException.class)
    public ResponseEntity<String> handleNotOpenedFacilityException(NotOpenedFacilityException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DeleteSpaceWithFacilitiesException.class)
    public ResponseEntity<String> handleDeleteSpaceWithFacilitiesException(DeleteSpaceWithFacilitiesException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidNumOfUserException.class)
    public ResponseEntity<String> handleInvalidNumOfUserException(InvalidNumOfUserException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LimitNumOfUserException.class)
    public ResponseEntity<String> handleLimitNumOfUserException(LimitNumOfUserException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
