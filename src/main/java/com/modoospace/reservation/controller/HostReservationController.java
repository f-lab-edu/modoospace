package com.modoospace.reservation.controller;

import com.modoospace.config.auth.LoginEmail;
import com.modoospace.reservation.controller.dto.ReservationResponse;
import com.modoospace.reservation.controller.dto.ReservationUpdateRequest;
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
@RequestMapping("/api/v1/hosts/reservations")
public class HostReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll(@LoginEmail final String loginEmail) {
        List<ReservationResponse> reservations = reservationService.findAllAsHost(loginEmail);
        return ResponseEntity.ok().body(reservations);
    }

    @PutMapping("/{reservationId}/approve")
    public ResponseEntity<Void> approveReservation(@PathVariable Long reservationId,
        @LoginEmail final String loginEmail) {
        reservationService.approveReservation(reservationId, loginEmail);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{reservationId}")
    public ResponseEntity<Void> update(
        @PathVariable Long reservationId,
        @RequestBody @Valid ReservationUpdateRequest updateRequest,
        @LoginEmail String loginEmail) {

        reservationService.updateReservation(reservationId, updateRequest, loginEmail);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> find(@PathVariable Long reservationId,
        @LoginEmail String loginEmail) {
        ReservationResponse reservation = reservationService
            .findReservation(reservationId, loginEmail);
        return ResponseEntity.ok().body(reservation);
    }
}
