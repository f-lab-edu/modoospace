package com.modoospace.reservation.controller.dto;

import com.modoospace.member.domain.Member;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.space.domain.Facility;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationReadDto {

  @NotNull
  private Long id;

  @NotBlank
  private LocalDateTime reservationStart;

  @NotBlank
  private LocalDateTime reservationEnd;

  @NotEmpty
  private ReservationStatus status;

  @NotEmpty
  private Facility facility;

  @NotEmpty
  private Member visitor;

  @Builder

  public ReservationReadDto(Long id, LocalDateTime reservationStart, LocalDateTime reservationEnd,
      ReservationStatus status, Facility facility, Member visitor) {
    this.id = id;
    this.reservationStart = reservationStart;
    this.reservationEnd = reservationEnd;
    this.status = status;
    this.facility = facility;
    this.visitor = visitor;
  }

  public static ReservationReadDto toDto(Reservation reservation){
    return ReservationReadDto.builder()
        .id(reservation.getId())
        .facility(reservation.getFacility())
        .reservationStart(reservation.getReservationStart())
        .reservationEnd(reservation.getReservationEnd())
        .status(reservation.getStatus())
        .visitor(reservation.getVisitor())
        .build();
  }
}
