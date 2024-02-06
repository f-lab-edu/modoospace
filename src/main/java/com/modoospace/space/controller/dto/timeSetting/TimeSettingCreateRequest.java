package com.modoospace.space.controller.dto.timeSetting;

import com.modoospace.space.domain.TimeRange;
import com.modoospace.space.domain.TimeSetting;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TimeSettingCreateRequest {

    @NotNull
    private Integer startHour = 0;

    @NotNull
    private Integer endHour = 24;

    public TimeSettingCreateRequest(Integer startHour, Integer endHour) {
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public TimeSetting toEntity() {
        return new TimeSetting(new TimeRange(startHour, endHour));
    }
}
