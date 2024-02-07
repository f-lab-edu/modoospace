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
public class TimeSettingResponse {

    @NotNull
    private Long id;

    @NotNull
    private Integer startHour;

    @NotNull
    private Integer endHour;

    @Builder
    public TimeSettingResponse(Long id, Integer startHour, Integer endHour) {
        this.id = id;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public static TimeSettingResponse of(TimeSetting timeSetting) {
        return TimeSettingResponse.builder()
            .id(timeSetting.getId())
            .startHour(timeSetting.getStartHour())
            .endHour(timeSetting.getEndHour())
            .build();
    }

    public static List<TimeSettingResponse> of(List<TimeSetting> timeSettings) {
        return timeSettings.stream()
            .map(TimeSettingResponse::of)
            .collect(Collectors.toList());
    }
}
