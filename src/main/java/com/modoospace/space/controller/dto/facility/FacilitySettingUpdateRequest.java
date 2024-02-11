package com.modoospace.space.controller.dto.facility;

import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateRequest;
import com.modoospace.space.controller.dto.weekdaySetting.WeekdaySettingCreateRequest;
import com.modoospace.space.domain.TimeSetting;
import com.modoospace.space.domain.TimeSettings;
import com.modoospace.space.domain.WeekdaySetting;
import com.modoospace.space.domain.WeekdaySettings;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;

public class FacilitySettingUpdateRequest {

    @NotEmpty
    private final List<TimeSettingCreateRequest> timeSettings;

    @NotEmpty
    private final List<WeekdaySettingCreateRequest> weekdaySettings;

    public FacilitySettingUpdateRequest(List<TimeSettingCreateRequest> timeSettings,
        List<WeekdaySettingCreateRequest> weekdaySettings) {
        this.timeSettings = timeSettings;
        this.weekdaySettings = weekdaySettings;
    }

    public TimeSettings toTimeSettings() {
        List<TimeSetting> entities = this.timeSettings.stream()
            .map(TimeSettingCreateRequest::toEntity)
            .collect(Collectors.toList());
        return new TimeSettings(entities);
    }

    public WeekdaySettings toWeekdaySettings() {
        List<WeekdaySetting> entities = this.weekdaySettings.stream()
            .map(WeekdaySettingCreateRequest::toEntity)
            .collect(Collectors.toList());
        return new WeekdaySettings(entities);
    }
}
