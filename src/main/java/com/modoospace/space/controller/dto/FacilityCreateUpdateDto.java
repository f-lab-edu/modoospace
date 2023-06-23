package com.modoospace.space.controller.dto;

import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityType;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.TimeSetting;
import com.modoospace.space.domain.Weekday;
import com.modoospace.space.domain.WeekdaySetting;
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
public class FacilityCreateUpdateDto {

  @NotEmpty
  private String name;

  @NotNull
  private FacilityType facilityType;

  @NotNull
  private Boolean reservationEnable;

  private String description;

  @Builder.Default
  private List<TimeSettingCreateDto> timeSettings = Arrays.asList(new TimeSettingCreateDto());

  @Builder.Default
  private List<WeekdaySettingCreateDto> weekdaySettings = Arrays.asList(
      new WeekdaySettingCreateDto(Weekday.SUN),
      new WeekdaySettingCreateDto(Weekday.MON),
      new WeekdaySettingCreateDto(Weekday.TUE),
      new WeekdaySettingCreateDto(Weekday.WED),
      new WeekdaySettingCreateDto(Weekday.THU),
      new WeekdaySettingCreateDto(Weekday.FRI),
      new WeekdaySettingCreateDto(Weekday.SAT)
  );

  public FacilityCreateUpdateDto(String name, FacilityType facilityType, Boolean reservationEnable,
      String description, List<TimeSettingCreateDto> timeSettings,
      List<WeekdaySettingCreateDto> weekdaySettings) {
    this.name = name;
    this.facilityType = facilityType;
    this.reservationEnable = reservationEnable;
    this.description = description;
    this.timeSettings = timeSettings;
    this.weekdaySettings = weekdaySettings;
  }

  public Facility toEntity(Space space) {
    return Facility.builder()
        .name(name)
        .facilityType(facilityType)
        .reservationEnable(reservationEnable)
        .description(description)
        .space(space)
        .timeSettings(toTimeSettings(timeSettings))
        .weekdaySettings(toWeekdaySettings(weekdaySettings))
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
