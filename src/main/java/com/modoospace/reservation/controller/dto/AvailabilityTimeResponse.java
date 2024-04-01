package com.modoospace.reservation.controller.dto;

import com.modoospace.space.controller.dto.facility.FacilityDetailResponse;
import java.util.List;
import lombok.Getter;

@Getter
public class AvailabilityTimeResponse {

    private final FacilityDetailResponse facility;
    private final List<TimeResponse> timeResponses;

    public AvailabilityTimeResponse(FacilityDetailResponse facility,
            List<TimeResponse> timeResponses) {
        this.facility = facility;
        this.timeResponses = timeResponses;
    }
}
