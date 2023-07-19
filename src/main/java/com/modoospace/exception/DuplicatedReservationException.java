package com.modoospace.exception;

public class DuplicatedReservationException extends RuntimeException{
  public DuplicatedReservationException(String message) {
    super(message);
  }
}
