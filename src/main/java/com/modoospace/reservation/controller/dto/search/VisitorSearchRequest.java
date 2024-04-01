package com.modoospace.reservation.controller.dto.search;

import com.modoospace.reservation.domain.ReservationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VisitorSearchRequest {

    private ReservationStatus status;
}
