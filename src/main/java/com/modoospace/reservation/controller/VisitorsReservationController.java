package com.modoospace.reservation.controller;

import com.modoospace.common.DateFormatManager;
import com.modoospace.config.auth.resolver.LoginMember;
import com.modoospace.member.domain.Member;
import com.modoospace.reservation.controller.dto.AvailabilityTimeResponse;
import com.modoospace.reservation.controller.dto.ReservationCreateRequest;
import com.modoospace.reservation.controller.dto.ReservationResponse;
import com.modoospace.reservation.serivce.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/visitors/reservations")
public class VisitorsReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll(@LoginMember Member loginMember) {
        List<ReservationResponse> reservations = reservationService.findAllAsVisitor(loginMember);
        return ResponseEntity.ok().body(reservations);
    }

    @GetMapping("/facilities/{facilityId}/availability")
    public ResponseEntity<AvailabilityTimeResponse> getAvailabilityTime(
            @PathVariable Long facilityId,
            @RequestParam @DateTimeFormat(pattern = DateFormatManager.DATE_FORMAT) final LocalDate date) {
        AvailabilityTimeResponse availableTimes = reservationService
                .getAvailabilityTime(facilityId, date);
        return ResponseEntity.ok().body(availableTimes);
    }

    @PostMapping("/facilities/{facilityId}")
    public ResponseEntity<Long> createReservation(@PathVariable Long facilityId,
                                                  @LoginMember Member loginMember,
                                                  @RequestBody @Valid ReservationCreateRequest createRequest) {
        Long reservationId = reservationService.createReservation(
                createRequest, facilityId, loginMember);
        return ResponseEntity.ok().body(reservationId);
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> find(@PathVariable Long reservationId,
                                                    @LoginMember Member loginMember) {
        ReservationResponse reservation = reservationService.findReservation(
                reservationId, loginMember);
        return ResponseEntity.ok().body(reservation);
    }

    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId,
                                                  @LoginMember Member loginMember) {
        reservationService.cancelReservation(reservationId, loginMember);
        return ResponseEntity.ok().build();
    }
}
