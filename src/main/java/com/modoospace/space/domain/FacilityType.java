package com.modoospace.space.domain;

import com.modoospace.reservation.domain.ReservationStatus;

public enum FacilityType {
  SEAT(ReservationStatus.COMPLETED),
  ROOM(ReservationStatus.WAITING);

  private final ReservationStatus defaultStatus;

  FacilityType(ReservationStatus defaultStatus) {
    this.defaultStatus = defaultStatus;
  }

  public ReservationStatus getDefaultStatus() {
    return defaultStatus;
  }
}
