package com.modoospace.reservation.controller;

import com.modoospace.config.auth.resolver.LoginMember;
import com.modoospace.member.domain.Member;
import com.modoospace.reservation.controller.dto.ReservationResponse;
import com.modoospace.reservation.controller.dto.ReservationUpdateRequest;
import com.modoospace.reservation.controller.dto.search.HostSearchRequest;
import com.modoospace.reservation.serivce.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/host/reservations")
public class HostReservationController {

    private final ReservationService reservationService;

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> find(@PathVariable Long reservationId,
                                                    @LoginMember Member loginMember) {
        ReservationResponse reservation = reservationService.findReservation(reservationId, loginMember);
        return ResponseEntity.ok().body(reservation);
    }

    @GetMapping()
    public ResponseEntity<Page<ReservationResponse>> search(HostSearchRequest searchRequest, Pageable pageable, @LoginMember Member loginMember) {
        Page<ReservationResponse> reservations = reservationService.searchReservationByHost(searchRequest, pageable, loginMember);
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
}
