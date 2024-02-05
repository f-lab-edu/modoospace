package com.modoospace.space.controller.dto.timeSetting;

import com.modoospace.space.domain.TimeSetting;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TimeSettingReadDto {

    @NotNull
    private Long id;

    @NotNull
    private Integer startHour;

    @NotNull
    private Integer endHour;

    @Builder
    public TimeSettingReadDto(Long id, Integer startHour, Integer endHour) {
        this.id = id;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public static TimeSettingReadDto toDto(TimeSetting timeSetting) {
        return TimeSettingReadDto.builder()
            .id(timeSetting.getId())
            .startHour(timeSetting.getStartHour())
            .endHour(timeSetting.getEndHour())
            .build();
    }

    public static List<TimeSettingReadDto> toDtos(List<TimeSetting> timeSettings) {
        return timeSettings.stream()
            .map(TimeSettingReadDto::toDto)
            .collect(Collectors.toList());
    }
}
