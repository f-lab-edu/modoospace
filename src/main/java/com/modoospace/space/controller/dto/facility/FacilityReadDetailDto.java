package com.modoospace.space.controller.dto.facility;

import com.modoospace.space.controller.dto.facilitySchedule.ScheduleReadDto;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingReadDto;
import com.modoospace.space.controller.dto.weekdaySetting.WeekdaySettingReadDto;
import com.modoospace.space.domain.Facility;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FacilityReadDetailDto {

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
    private List<TimeSettingReadDto> timeSettings;

    @NotEmpty
    private List<WeekdaySettingReadDto> weekdaySettings;

    @NotEmpty
    private List<ScheduleReadDto> facilitySchedules;

    @Builder
    public FacilityReadDetailDto(Long id, String name, Boolean reservationEnable,
        Integer minUser, Integer maxUser, String description,
        List<TimeSettingReadDto> timeSettings,
        List<WeekdaySettingReadDto> weekdaySettings,
        List<ScheduleReadDto> facilitySchedules) {
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

    public static FacilityReadDetailDto toDto(Facility facility) {
        return FacilityReadDetailDto.builder()
            .id(facility.getId())
            .name(facility.getName())
            .reservationEnable(facility.getReservationEnable())
            .minUser(facility.getMinUser())
            .maxUser(facility.getMaxUser())
            .description(facility.getDescription())
            .timeSettings(TimeSettingReadDto.toDtos(facility.getTimeSettings().getTimeSettings()))
            .weekdaySettings(
                WeekdaySettingReadDto.toDtos(facility.getWeekdaySettings().getWeekdaySettings()))
            .facilitySchedules(ScheduleReadDto.toDtos(facility.getSchedules()
                .getSchedules()))
            .build();
    }
}
