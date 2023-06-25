package com.modoospace.reservation.controller;

import com.modoospace.config.auth.LoginEmail;
import com.modoospace.reservation.controller.dto.ReservationReadDto;
import com.modoospace.reservation.controller.dto.ReservationUpdateDto;
import com.modoospace.reservation.serivce.ReservationService;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/reservations")
public class AdminReservationController {

  private final ReservationService reservationService;

  @GetMapping
  public ResponseEntity<List<ReservationReadDto>> findAll(
      @LoginEmail final String loginEmail) {
    List<ReservationReadDto> reservationList = reservationService.findAll(loginEmail);
    return ResponseEntity.ok().body(reservationList);
  }

  @PutMapping("/{reservationId}")
  public ResponseEntity<Void> update(
      @PathVariable Long reservationId,
      @RequestBody @Valid ReservationUpdateDto reservationCreateDto,
      @LoginEmail String loginEmail) {
    reservationService.updateReservation(reservationId, reservationCreateDto, loginEmail);
    return ResponseEntity.ok().build();
  }
}
