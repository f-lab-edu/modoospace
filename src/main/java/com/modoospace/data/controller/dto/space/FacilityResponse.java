package com.modoospace.data.controller.dto.space;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.TimeSettings;
import com.modoospace.space.domain.WeekdaySettings;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacilityResponse {

    FacilityInfo info;
    List<FacilityCategory> categories;

    public Facility toFacility(TimeSettings timeSettings, WeekdaySettings weekdaySettings, Space space) {
        return Facility.builder()
                .name(info.getName())
                .reservationEnable(true)
                .minUser(info.getMinUser())
                .maxUser(info.getMaxUser())
                .description(info.getDescription())
                .space(space)
                .timeSettings(timeSettings)
                .weekdaySettings(weekdaySettings)
                .build();
    }
}
