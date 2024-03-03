package com.modoospace.space.controller.dto.facility;

import com.modoospace.space.controller.dto.timeSetting.TimeSettingResponse;
import com.modoospace.space.controller.dto.weekdaySetting.WeekdaySettingResponse;
import com.modoospace.space.domain.Facility;
import java.util.List;
import java.util.stream.Collectors;
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

    @NotEmpty
    private List<TimeSettingResponse> timeSettings;

    @NotEmpty
    private List<WeekdaySettingResponse> weekdaySettings;

    @Builder
    public FacilityResponse(Long id, String name, Boolean reservationEnable, Integer minUser,
        Integer maxUser, String description, List<TimeSettingResponse> timeSettings,
        List<WeekdaySettingResponse> weekdaySettings) {
        this.id = id;
        this.name = name;
        this.reservationEnable = reservationEnable;

        this.minUser = minUser;
        this.maxUser = maxUser;
        this.description = description;

        this.timeSettings = timeSettings;
        this.weekdaySettings = weekdaySettings;
    }

    public static FacilityResponse of(Facility facility) {
        return FacilityResponse.builder()
            .id(facility.getId())
            .name(facility.getName())
            .reservationEnable(facility.getReservationEnable())
            .minUser(facility.getMinUser())
            .maxUser(facility.getMaxUser())
            .description(facility.getDescription())
            .timeSettings(TimeSettingResponse
                .of(facility.getTimeSettings().getTimeSettings()))
            .weekdaySettings(WeekdaySettingResponse
                .of(facility.getWeekdaySettings().getWeekdaySettings()))
            .build();
    }

    public static List<FacilityResponse> of(List<Facility> facilities) {
        return facilities.stream()
            .map(FacilityResponse::of)
            .collect(Collectors.toList());
    }
}
