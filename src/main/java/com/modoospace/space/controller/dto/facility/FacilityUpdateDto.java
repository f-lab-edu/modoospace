package com.modoospace.space.controller.dto.facility;

import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateDto;
import com.modoospace.space.controller.dto.weekdaySetting.WeekdaySettingCreateDto;
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
public class FacilityUpdateDto {

  @NotEmpty
  private String name;

  @NotNull
  private Boolean reservationEnable;

  private String description;

  @Builder.Default
  private List<TimeSettingCreateDto> timeSettings = new ArrayList<>();

  @Builder.Default
  private List<WeekdaySettingCreateDto> weekdaySettings = new ArrayList<>();

  public FacilityUpdateDto(String name, Boolean reservationEnable, String description,
      List<TimeSettingCreateDto> timeSettings, List<WeekdaySettingCreateDto> weekdaySettings) {
    this.name = name;
    this.reservationEnable = reservationEnable;
    this.description = description;
    this.timeSettings = timeSettings;
    this.weekdaySettings = weekdaySettings;
  }

  public Facility toEntity() {
    return Facility.builder()
        .name(name)
        .reservationEnable(reservationEnable)
        .description(description)
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
