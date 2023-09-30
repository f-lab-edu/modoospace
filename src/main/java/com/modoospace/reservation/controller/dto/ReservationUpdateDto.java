package com.modoospace.reservation.controller.dto;

import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationUpdateDto {

  ReservationStatus status;

  public ReservationUpdateDto(ReservationStatus status) {
    this.status = status;
  }

  public Reservation toEntity(Reservation reservation) {
    return new Reservation(reservation.getReservationStart(),
        reservation.getReservationEnd(),
        reservation.getFacility(),
        reservation.getVisitor());
  }
}
