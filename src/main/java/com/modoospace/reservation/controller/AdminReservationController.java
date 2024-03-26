package com.modoospace.reservation.controller;

import com.modoospace.config.auth.aop.CheckLogin;
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
@RequestMapping("/api/v1/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    @CheckLogin
    @GetMapping("/visitor/{memberId}")
    public ResponseEntity<List<ReservationResponse>> findAllAsMember(@PathVariable Long memberId,
                                                                     @LoginMember Member loginMember) {
        List<ReservationResponse> reservations = reservationService
                .findAllAsVisitorByAdmin(memberId, loginMember);
        return ResponseEntity.ok().body(reservations);
    }

    @CheckLogin
    @GetMapping("/host/{memberId}")
    public ResponseEntity<List<ReservationResponse>> findAllAsHost(@PathVariable Long memberId,
                                                                   @LoginMember Member loginMember) {
        List<ReservationResponse> reservations = reservationService
                .findAllAsHostByAdmin(memberId, loginMember);
        return ResponseEntity.ok().body(reservations);
    }

    @CheckLogin
    @PutMapping("/{reservationId}")
    public ResponseEntity<Void> update(
            @PathVariable Long reservationId,
            @RequestBody @Valid ReservationUpdateRequest updateRequest,
            @LoginMember Member loginMember) {

        reservationService.updateReservation(reservationId, updateRequest, loginMember);
        return ResponseEntity.ok().build();
    }

    @CheckLogin
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> find(@PathVariable Long reservationId,
                                                    @LoginMember Member loginMember) {
        ReservationResponse reservation = reservationService
                .findReservation(reservationId, loginMember);
        return ResponseEntity.ok().body(reservation);
    }
}
