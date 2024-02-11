package com.modoospace.space.controller.dto.facility;

import com.modoospace.space.domain.Facility;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FacilityUpdateRequest {

    @NotEmpty
    private String name;

    @NotNull
    private Boolean reservationEnable;

    @NotNull
    private Integer minUser;

    @NotNull
    private Integer maxUser;

    private String description;

    @Builder
    public FacilityUpdateRequest(String name, Boolean reservationEnable, Integer minUser,
        Integer maxUser, String description) {
        this.name = name;
        this.reservationEnable = reservationEnable;
        this.minUser = minUser;
        this.maxUser = maxUser;
        this.description = description;
    }

    public Facility toEntity() {
        return Facility.builder()
            .name(name)
            .reservationEnable(reservationEnable)
            .minUser(minUser)
            .maxUser(maxUser)
            .description(description)
            .build();
    }
}
