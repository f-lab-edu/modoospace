package com.modoospace.reservation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modoospace.member.domain.Member;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.space.domain.Facility;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationCreateDto {

  @NotNull
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime reservationStart;

  @NotNull
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime reservationEnd;

  @Builder
  public ReservationCreateDto(LocalDateTime reservationStart,
      LocalDateTime reservationEnd) {
    this.reservationStart = reservationStart;
    this.reservationEnd = reservationEnd;
  }

  public Reservation toEntity(Facility facility, Member visitor) {
    return Reservation.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
        .visitor(visitor)
        .facility(facility)
        .build();
  }
}
