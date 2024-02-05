package com.modoospace.space.controller.dto.facility;

import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateDto;
import com.modoospace.space.controller.dto.weekdaySetting.WeekdaySettingCreateDto;
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
public class FacilityCreateDto {

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
    private List<TimeSettingCreateDto> timeSettings = Arrays.asList(new TimeSettingCreateDto());

    @Builder.Default
    private List<WeekdaySettingCreateDto> weekdaySettings = Arrays.asList(
        new WeekdaySettingCreateDto(DayOfWeek.MONDAY),
        new WeekdaySettingCreateDto(DayOfWeek.TUESDAY),
        new WeekdaySettingCreateDto(DayOfWeek.WEDNESDAY),
        new WeekdaySettingCreateDto(DayOfWeek.THURSDAY),
        new WeekdaySettingCreateDto(DayOfWeek.FRIDAY),
        new WeekdaySettingCreateDto(DayOfWeek.SATURDAY),
        new WeekdaySettingCreateDto(DayOfWeek.SUNDAY)
    );

    public FacilityCreateDto(String name, Boolean reservationEnable,
        Integer minUser, Integer maxUser, String description,
        List<TimeSettingCreateDto> timeSettings, List<WeekdaySettingCreateDto> weekdaySettings) {
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

    private List<TimeSetting> toTimeSettings(List<TimeSettingCreateDto> timeSettings) {
        return timeSettings.stream()
            .map(settingCreateDto -> settingCreateDto.toEntity())
            .collect(Collectors.toList());
    }

    private List<WeekdaySetting> toWeekdaySettings(List<WeekdaySettingCreateDto> weekdaySettings) {
        return weekdaySettings.stream()
            .map(settingCreateDto -> settingCreateDto.toEntity())
            .collect(Collectors.toList());
    }
}
