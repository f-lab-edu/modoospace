package com.modoospace.reservation.controller.dto;

import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationUpdateDto {

  ReservationStatus status;

  @Builder
  public ReservationUpdateDto(ReservationStatus status) {
    this.status = status;
  }

  public Reservation toEntity(Reservation reservation) {
    return Reservation.builder()
        .reservationStart(reservation.getReservationStart())
        .reservationEnd(reservation.getReservationEnd())
        .status(status)
        .visitor(reservation.getVisitor())
        .facility(reservation.getFacility())
        .build();
  }
}
