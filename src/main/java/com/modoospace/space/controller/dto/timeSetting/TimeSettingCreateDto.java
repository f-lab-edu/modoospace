package com.modoospace.space.controller.dto.timeSetting;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modoospace.common.DateFormatManager;
import com.modoospace.space.domain.TimeRange;
import com.modoospace.space.domain.TimeSetting;
import java.time.LocalTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TimeSettingCreateDto {

    @NotNull
    private Integer startHour = 0;

    @NotNull
    private Integer endHour = 24;

    public TimeSettingCreateDto(Integer startHour, Integer endHour) {
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public TimeSetting toEntity() {
        return TimeSetting.builder()
            .timeRange(new TimeRange(startHour, endHour))
            .build();
    }
}
