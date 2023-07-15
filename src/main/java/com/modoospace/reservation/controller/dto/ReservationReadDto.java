package com.modoospace.reservation.controller.dto;

import com.modoospace.member.controller.dto.MemberReadDto;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.space.controller.dto.facility.FacilityReadDto;
import com.modoospace.space.domain.Facility;
import java.time.LocalDateTime;
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

  @NotNull
  private LocalDateTime reservationStart;

  @NotNull
  private LocalDateTime reservationEnd;

  @NotEmpty
  private ReservationStatus status;

  @NotEmpty
  private FacilityReadDto facility;

  @NotEmpty
  private MemberReadDto member;

  @Builder
  public ReservationReadDto(Long id, LocalDateTime reservationStart, LocalDateTime reservationEnd,
      ReservationStatus status, FacilityReadDto facility, MemberReadDto member) {
    this.id = id;
    this.reservationStart = reservationStart;
    this.reservationEnd = reservationEnd;
    this.status = status;
    this.facility = facility;
    this.member = member;
  }

  public static ReservationReadDto toDto(Reservation reservation){
    return ReservationReadDto.builder()
        .id(reservation.getId())
        .facility(FacilityReadDto.toDto(reservation.getFacility()))
        .reservationStart(reservation.getReservationStart())
        .reservationEnd(reservation.getReservationEnd())
        .status(reservation.getStatus())
        .member(MemberReadDto.toDto(reservation.getVisitor()))
        .build();
  }
}
