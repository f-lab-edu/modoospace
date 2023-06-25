package com.modoospace.reservation.controller.dto;

import com.modoospace.member.domain.Member;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityType;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationCreateDto {

  @NotBlank
  private LocalDateTime reservationStart;

  @NotBlank
  private LocalDateTime reservationEnd;

  @Builder
  public ReservationCreateDto(LocalDateTime reservationStart,
      LocalDateTime reservationEnd) {
    this.reservationStart = reservationStart;
    this.reservationEnd = reservationEnd;
  }

  public Reservation toEntity(Facility facility, Member visitor) {
    ReservationStatus reservationStatus = setReservationStatusByFacilityType(facility.getFacilityType());

    return Reservation.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
        .status(reservationStatus)
        .visitor(visitor)
        .facility(facility)
        .build();
  }

  private ReservationStatus setReservationStatusByFacilityType(FacilityType facilityType) {
    if (facilityType == FacilityType.SEAT) {
      return ReservationStatus.COMPLETED;
    } else if (facilityType == FacilityType.ROOM) {
      return ReservationStatus.WAITING;
    } else {
      return ReservationStatus.WAITING;
    }
  }

}
