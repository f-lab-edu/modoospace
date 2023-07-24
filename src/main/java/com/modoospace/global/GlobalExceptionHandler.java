package com.modoospace.global;

import com.modoospace.global.exception.AlarmSendException;
import com.modoospace.global.exception.ConflictingReservationException;
import com.modoospace.global.exception.ConflictingTimeException;
import com.modoospace.global.exception.DuplicatedWeekdayException;
import com.modoospace.global.exception.InvalidTimeRangeException;
import com.modoospace.global.exception.NotFoundEntityException;
import com.modoospace.global.exception.NotOpenedFacilityException;
import com.modoospace.global.exception.PermissionDeniedException;
import com.modoospace.global.exception.SSEConnectError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AlarmSendException.class)
  public ResponseEntity<String> handleAlarmSendException(
      InvalidTimeRangeException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ConflictingReservationException.class)
  public ResponseEntity<String> handleConflictingReservationException(
      ConflictingReservationException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
  }

  @ExceptionHandler(ConflictingTimeException.class)
  public ResponseEntity<String> handleConflictingTimeException(
      ConflictingTimeException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
  }

  @ExceptionHandler(DuplicatedWeekdayException.class)
  public ResponseEntity<String> handleDuplicatedWeekdayException(
      ConflictingTimeException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
  }

  @ExceptionHandler(InvalidTimeRangeException.class)
  public ResponseEntity<String> handleInvalidTimeRangeException(
      InvalidTimeRangeException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotFoundEntityException.class)
  public ResponseEntity<String> handleNotFoundEntityException(
      NotFoundEntityException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(NotOpenedFacilityException.class)
  public ResponseEntity<String> handleNotOpenedFacilityException(
      NotOpenedFacilityException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(PermissionDeniedException.class)
  public ResponseEntity<String> handlePermissionDeniedException(
      PermissionDeniedException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(SSEConnectError.class)
  public ResponseEntity<String> handleSSEConnectError(
      SSEConnectError exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
