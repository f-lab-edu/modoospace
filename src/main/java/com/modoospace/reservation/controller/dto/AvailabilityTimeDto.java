package com.modoospace.reservation.controller.dto;

import com.modoospace.space.controller.dto.facility.FacilityReadDto;
import java.util.List;
import lombok.Getter;

@Getter
public class AvailabilityTimeDto {

    private final FacilityReadDto facility;
    private final List<TimeResponse> timeResponses;

    public AvailabilityTimeDto(FacilityReadDto facility, List<TimeResponse> timeResponses) {
        this.facility = facility;
        this.timeResponses = timeResponses;
    }
}
