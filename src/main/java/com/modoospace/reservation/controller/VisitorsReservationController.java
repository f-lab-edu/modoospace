package com.modoospace.reservation.controller;

import com.modoospace.common.DayDto;
import com.modoospace.config.auth.LoginEmail;
import com.modoospace.reservation.controller.dto.AvailabilityNowResponseDto;
import com.modoospace.reservation.controller.dto.AvailabilityTimeResponseDto;
import com.modoospace.reservation.controller.dto.ReservationCreateDto;
import com.modoospace.reservation.controller.dto.ReservationReadDto;
import com.modoospace.reservation.serivce.ReservationService;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/visitors/reservations")
public class VisitorsReservationController {

  private final ReservationService reservationService;

  @GetMapping
  public ResponseEntity<List<ReservationReadDto>> findAllReservation(
      @LoginEmail final String loginEmail) {
    List<ReservationReadDto> reservationList = reservationService.findAllAsVisitor(loginEmail);
    return ResponseEntity.ok().body(reservationList);
  }

  @GetMapping("/facilities/{facilityId}/availability/now")
  public ResponseEntity<AvailabilityNowResponseDto> getAvailabilityNow(
      @PathVariable Long facilityId) {
    AvailabilityNowResponseDto availabilityNow = reservationService
        .getAvailabilityNow(facilityId);
    return ResponseEntity.ok().body(availabilityNow);
  }

  @GetMapping("/facilities/{facilityId}/availability")
  public ResponseEntity<AvailabilityTimeResponseDto> getAvailabilityTime(
      @PathVariable Long facilityId,
      @RequestBody @Valid DayDto searchDto) {
    AvailabilityTimeResponseDto availableTimes = reservationService
        .getAvailabilityTime(facilityId, searchDto.getDate());
    return ResponseEntity.ok().body(availableTimes);
  }

  @PostMapping("/facilities/{facilityId}")
  public ResponseEntity<Long> createReservation(@PathVariable Long facilityId,
      @LoginEmail String loginEmail,
      @RequestBody @Valid ReservationCreateDto createDto) {
    Long reservation = reservationService.createReservation(
        createDto, facilityId, loginEmail);
    return ResponseEntity.ok().body(reservation);
  }

  @GetMapping("/{reservationId}")
  public ResponseEntity<ReservationReadDto> find(@PathVariable Long reservationId,
      @LoginEmail String loginEmail) {
    ReservationReadDto reservationReadDto = reservationService.findReservation(
        reservationId, loginEmail);
    return ResponseEntity.ok().body(reservationReadDto);
  }

  @PutMapping("/{reservationId}/cancel")
  public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId,
      @LoginEmail String loginEmail) {
    reservationService.cancelReservation(reservationId, loginEmail);
    return ResponseEntity.ok().build();
  }
}
