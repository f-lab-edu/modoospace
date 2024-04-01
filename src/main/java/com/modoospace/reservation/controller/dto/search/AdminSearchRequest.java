package com.modoospace.reservation.controller.dto.search;

import com.modoospace.reservation.domain.ReservationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminSearchRequest {

    private Long visitorId;

    private Long hostId;

    private Long spaceId;

    private String spaceName;

    private ReservationStatus status;
}
