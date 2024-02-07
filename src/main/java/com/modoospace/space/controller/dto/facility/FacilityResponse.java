package com.modoospace.space.controller.dto.facility;

import com.modoospace.space.domain.Facility;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FacilityResponse {

    @NotNull
    private Long id;

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
    public FacilityResponse(Long id, String name, Boolean reservationEnable,
        Integer minUser, Integer maxUser, String description) {
        this.id = id;
        this.name = name;
        this.reservationEnable = reservationEnable;

        this.minUser = minUser;
        this.maxUser = maxUser;
        this.description = description;
    }

    public static FacilityResponse of(Facility facility) {
        return FacilityResponse.builder()
            .id(facility.getId())
            .name(facility.getName())
            .reservationEnable(facility.getReservationEnable())
            .minUser(facility.getMinUser())
            .maxUser(facility.getMaxUser())
            .description(facility.getDescription())
            .build();
    }
}
