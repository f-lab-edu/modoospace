package com.modoospace.space.controller.dto.facility;

import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateRequest;
import com.modoospace.space.controller.dto.weekdaySetting.WeekdaySettingCreateRequest;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.TimeSetting;
import com.modoospace.space.domain.TimeSettings;
import com.modoospace.space.domain.WeekdaySetting;
import com.modoospace.space.domain.WeekdaySettings;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
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

    @Builder.Default
    private List<TimeSettingCreateRequest> timeSettings = new ArrayList<>();

    @Builder.Default
    private List<WeekdaySettingCreateRequest> weekdaySettings = new ArrayList<>();

    public FacilityUpdateRequest(String name, Boolean reservationEnable,
        Integer minUser, Integer maxUser, String description,
        List<TimeSettingCreateRequest> timeSettings, List<WeekdaySettingCreateRequest> weekdaySettings) {
        this.name = name;
        this.reservationEnable = reservationEnable;

        this.minUser = minUser;
        this.maxUser = maxUser;
        this.description = description;

        this.timeSettings = timeSettings;
        this.weekdaySettings = weekdaySettings;
    }

    public Facility toEntity() {
        return Facility.builder()
            .name(name)
            .reservationEnable(reservationEnable)
            .minUser(minUser)
            .maxUser(maxUser)
            .description(description)
            .timeSettings(new TimeSettings(toTimeSettings(timeSettings)))
            .weekdaySettings(new WeekdaySettings(toWeekdaySettings(weekdaySettings)))
            .build();
    }

    private List<TimeSetting> toTimeSettings(List<TimeSettingCreateRequest> timeSettings) {
        return timeSettings.stream()
            .map(settingcreateRequest -> settingcreateRequest.toEntity())
            .collect(Collectors.toList());
    }

    private List<WeekdaySetting> toWeekdaySettings(List<WeekdaySettingCreateRequest> weekdaySettings) {
        return weekdaySettings.stream()
            .map(settingcreateRequest -> settingcreateRequest.toEntity())
            .collect(Collectors.toList());
    }
}
