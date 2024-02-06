package com.modoospace.space.controller.dto.facility;

import com.modoospace.space.controller.dto.schedule.ScheduleResponse;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingResponse;
import com.modoospace.space.controller.dto.weekdaySetting.WeekdaySettingResponse;
import com.modoospace.space.domain.Facility;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FacilityDetailResponse {

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

    @NotEmpty
    private List<ScheduleResponse> facilitySchedules;

    @Builder
    public FacilityDetailResponse(Long id, String name, Boolean reservationEnable,
        Integer minUser, Integer maxUser, String description,
        List<TimeSettingResponse> timeSettings,
        List<WeekdaySettingResponse> weekdaySettings,
        List<ScheduleResponse> facilitySchedules) {
        this.id = id;
        this.name = name;
        this.reservationEnable = reservationEnable;

        this.minUser = minUser;
        this.maxUser = maxUser;
        this.description = description;

        this.timeSettings = timeSettings;
        this.weekdaySettings = weekdaySettings;
        this.facilitySchedules = facilitySchedules;
    }

    public static FacilityDetailResponse of(Facility facility) {
        return FacilityDetailResponse.builder()
            .id(facility.getId())
            .name(facility.getName())
            .reservationEnable(facility.getReservationEnable())
            .minUser(facility.getMinUser())
            .maxUser(facility.getMaxUser())
            .description(facility.getDescription())
            .timeSettings(TimeSettingResponse.of(facility.getTimeSettings().getTimeSettings()))
            .weekdaySettings(
                WeekdaySettingResponse.of(facility.getWeekdaySettings().getWeekdaySettings()))
            .facilitySchedules(ScheduleResponse.of(facility.getSchedules()
                .getSchedules()))
            .build();
    }
}
