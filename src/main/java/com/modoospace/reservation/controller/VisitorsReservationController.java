package com.modoospace.reservation.controller;

import com.modoospace.common.DateFormatManager;
import com.modoospace.config.auth.resolver.LoginMember;
import com.modoospace.member.domain.Member;
import com.modoospace.reservation.controller.dto.AvailabilityTimeResponse;
import com.modoospace.reservation.controller.dto.ReservationCreateRequest;
import com.modoospace.reservation.controller.dto.ReservationResponse;
import com.modoospace.reservation.controller.dto.search.VisitorSearchRequest;
import com.modoospace.reservation.serivce.ReservationService;
import java.time.LocalDate;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/visitor/reservations")
public class VisitorsReservationController {

    private final ReservationService reservationService;

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> find(@PathVariable Long reservationId,
            @LoginMember Member loginMember) {
        ReservationResponse reservation = reservationService.findReservation(reservationId,
                loginMember);
        return ResponseEntity.ok().body(reservation);
    }

    @GetMapping()
    public ResponseEntity<Page<ReservationResponse>> search(VisitorSearchRequest searchRequest,
            Pageable pageable,
            @LoginMember Member loginMember) {
        Page<ReservationResponse> reservations = reservationService.searchReservationByVisitor(
                searchRequest, pageable, loginMember);
        return ResponseEntity.ok().body(reservations);
    }

    @GetMapping("/facilities/{facilityId}/availability")
    public ResponseEntity<AvailabilityTimeResponse> getAvailabilityTime(
            @PathVariable Long facilityId,
            @RequestParam @DateTimeFormat(pattern = DateFormatManager.DATE_FORMAT) final LocalDate date) {
        AvailabilityTimeResponse availableTimes = reservationService.getAvailabilityTime(facilityId,
                date);
        return ResponseEntity.ok().body(availableTimes);
    }

    @PostMapping("/facilities/{facilityId}")
    public ResponseEntity<Long> createReservation(@PathVariable Long facilityId,
            @LoginMember Member loginMember,
            @RequestBody @Valid ReservationCreateRequest createRequest) {
        Long reservationId = reservationService.createReservation(createRequest, facilityId,
                loginMember);
        return ResponseEntity.ok().body(reservationId);
    }

    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId,
            @LoginMember Member loginMember) {
        reservationService.cancelReservation(reservationId, loginMember);
        return ResponseEntity.ok().build();
    }
}
