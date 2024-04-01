package com.modoospace.space.controller.dto.facility;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FacilitySearchRequest {

    private String name;

    private Boolean reservationEnable;
}
