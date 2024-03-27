package com.modoospace.reservation.controller;

import com.modoospace.config.auth.resolver.LoginMember;
import com.modoospace.member.domain.Member;
import com.modoospace.reservation.controller.dto.ReservationResponse;
import com.modoospace.reservation.controller.dto.ReservationUpdateRequest;
import com.modoospace.reservation.serivce.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/hosts/reservations")
public class HostReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll(@LoginMember Member loginMember) {
        List<ReservationResponse> reservations = reservationService.findAllAsHost(loginMember);
        return ResponseEntity.ok().body(reservations);
    }

    @PutMapping("/{reservationId}/approve")
    public ResponseEntity<Void> approveReservation(@PathVariable Long reservationId,
                                                   @LoginMember Member loginMember) {
        reservationService.approveReservation(reservationId, loginMember);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{reservationId}")
    public ResponseEntity<Void> update(
            @PathVariable Long reservationId,
            @RequestBody @Valid ReservationUpdateRequest updateRequest,
            @LoginMember Member loginMember) {

        reservationService.updateReservation(reservationId, updateRequest, loginMember);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> find(@PathVariable Long reservationId,
                                                    @LoginMember Member loginMember) {
        ReservationResponse reservation = reservationService.findReservation(reservationId, loginMember);
        return ResponseEntity.ok().body(reservation);
    }
}
