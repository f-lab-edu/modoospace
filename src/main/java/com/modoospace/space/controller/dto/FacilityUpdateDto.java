package com.modoospace.space.controller.dto;

import com.modoospace.space.domain.Facility;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@Builder
public class FacilityUpdateDto {

    @NotEmpty
    private String name;

    @NotNull
    private Boolean reservationEnable;

    private String description;

    public FacilityUpdateDto(String name, Boolean reservationEnable, String description) {
        this.name = name;
        this.reservationEnable = reservationEnable;
        this.description = description;
    }

    public Facility toEntity() {
        return Facility.builder()
            .name(name)
            .reservationEnable(reservationEnable)
            .description(description)
            .build();
    }
}
