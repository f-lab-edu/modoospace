package com.modoospace.space.controller.dto.facility;

import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateRequest;
import com.modoospace.space.controller.dto.weekdaySetting.WeekdaySettingCreateRequest;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.TimeSetting;
import com.modoospace.space.domain.TimeSettings;
import com.modoospace.space.domain.WeekdaySetting;
import com.modoospace.space.domain.WeekdaySettings;
import java.time.DayOfWeek;
import java.util.Arrays;
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
public class FacilityCreateRequest {

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
    private List<TimeSettingCreateRequest> timeSettings = Arrays.asList(new TimeSettingCreateRequest());

    @Builder.Default
    private List<WeekdaySettingCreateRequest> weekdaySettings = Arrays.asList(
        new WeekdaySettingCreateRequest(DayOfWeek.MONDAY),
        new WeekdaySettingCreateRequest(DayOfWeek.TUESDAY),
        new WeekdaySettingCreateRequest(DayOfWeek.WEDNESDAY),
        new WeekdaySettingCreateRequest(DayOfWeek.THURSDAY),
        new WeekdaySettingCreateRequest(DayOfWeek.FRIDAY),
        new WeekdaySettingCreateRequest(DayOfWeek.SATURDAY),
        new WeekdaySettingCreateRequest(DayOfWeek.SUNDAY)
    );

    public FacilityCreateRequest(String name, Boolean reservationEnable,
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

    public Facility toEntity(Space space) {
        return Facility.builder()
            .name(name)
            .reservationEnable(reservationEnable)
            .minUser(minUser)
            .maxUser(maxUser)
            .description(description)
            .space(space)
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
