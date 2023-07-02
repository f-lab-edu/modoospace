package com.modoospace.reservation.controller.dto;

import com.modoospace.member.domain.Member;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.space.domain.Facility;
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

  public Reservation toEntity(Facility facility, Member visitor){
    return Reservation.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
        .visitor(visitor)
        .facility(facility)
        .build();
  }
}
