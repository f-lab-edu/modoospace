package com.modoospace.reservation.controller;

import com.modoospace.config.auth.resolver.LoginMember;
import com.modoospace.member.domain.Member;
import com.modoospace.reservation.controller.dto.ReservationResponse;
import com.modoospace.reservation.controller.dto.ReservationUpdateRequest;
import com.modoospace.reservation.controller.dto.search.AdminSearchRequest;
import com.modoospace.reservation.serivce.ReservationService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> find(@PathVariable Long reservationId,
            @LoginMember Member loginMember) {
        ReservationResponse reservation = reservationService.findReservation(reservationId,
                loginMember);
        return ResponseEntity.ok().body(reservation);
    }

    @GetMapping("")
    public ResponseEntity<Page<ReservationResponse>> search(AdminSearchRequest searchRequest,
            Pageable pageable,
            @LoginMember Member loginMember) {
        Page<ReservationResponse> reservations = reservationService.searchReservationByAdmin(
                searchRequest, pageable, loginMember);
        return ResponseEntity.ok().body(reservations);
    }

    @PutMapping("/{reservationId}")
    public ResponseEntity<Void> update(@PathVariable Long reservationId,
            @RequestBody @Valid ReservationUpdateRequest updateRequest,
            @LoginMember Member loginMember) {

        reservationService.updateReservation(reservationId, updateRequest, loginMember);
        return ResponseEntity.ok().build();
    }
}
