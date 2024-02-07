package com.modoospace.reservation.controller.dto;

import com.modoospace.space.controller.dto.facility.FacilityResponse;
import java.util.List;
import lombok.Getter;

@Getter
public class AvailabilityTimeResponse {

    private final FacilityResponse facility;
    private final List<TimeResponse> timeResponses;

    public AvailabilityTimeResponse(FacilityResponse facility, List<TimeResponse> timeResponses) {
        this.facility = facility;
        this.timeResponses = timeResponses;
    }
}
