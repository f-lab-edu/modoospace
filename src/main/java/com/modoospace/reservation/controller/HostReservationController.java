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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/hosts/reservations")
public class HostReservationController {

  private final ReservationService reservationService;

  @GetMapping
  public ResponseEntity<List<ReservationReadDto>> findAll(@LoginEmail final String loginEmail) {
    List<ReservationReadDto> reservationList = reservationService.findAll(loginEmail);
    return ResponseEntity.ok().body(reservationList);
  }

  @PostMapping("/{reservationId}/approve")
  public ResponseEntity<Void> approveReservation(@PathVariable Long reservationId) {
    reservationService.updateStatus(reservationId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{reservationId}")
  public ResponseEntity<Void> update(
      @PathVariable Long reservationId,
      @RequestBody @Valid ReservationUpdateDto reservationUpdateDto,
      @LoginEmail String loginEmail) {

    reservationService.updateReservation(reservationId, reservationUpdateDto, loginEmail);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{reservationId}")
  public ResponseEntity<ReservationReadDto> find(@PathVariable Long reservationId){
    ReservationReadDto reservationReadDto = reservationService.findReservation(reservationId);
    return ResponseEntity.ok().body(reservationReadDto);
  }
}
