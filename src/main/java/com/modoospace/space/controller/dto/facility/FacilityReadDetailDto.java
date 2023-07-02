package com.modoospace.space.controller.dto.facility;

import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleReadDto;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingReadDto;
import com.modoospace.space.controller.dto.weekdaySetting.WeekdaySettingReadDto;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityType;
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
  private FacilityType facilityType;

  @NotNull
  private Boolean reservationEnable;

  private String description;

  @NotEmpty
  private List<TimeSettingReadDto> timeSettings;

  @NotEmpty
  private List<WeekdaySettingReadDto> weekdaySettings;

  @NotEmpty
  private List<FacilityScheduleReadDto> facilitySchedules;

  @Builder
  public FacilityReadDetailDto(Long id, String name, FacilityType facilityType,
      Boolean reservationEnable, String description, List<TimeSettingReadDto> timeSettings,
      List<WeekdaySettingReadDto> weekdaySettings,
      List<FacilityScheduleReadDto> facilitySchedules) {
    this.id = id;
    this.name = name;
    this.facilityType = facilityType;
    this.reservationEnable = reservationEnable;
    this.description = description;
    this.timeSettings = timeSettings;
    this.weekdaySettings = weekdaySettings;
    this.facilitySchedules = facilitySchedules;
  }

  public static FacilityReadDetailDto toDto(Facility facility) {
    return FacilityReadDetailDto.builder()
        .id(facility.getId())
        .name(facility.getName())
        .facilityType(facility.getFacilityType())
        .reservationEnable(facility.getReservationEnable())
        .description(facility.getDescription())
        .timeSettings(TimeSettingReadDto.toDtos(facility.getTimeSettings().getTimeSettings()))
        .weekdaySettings(
            WeekdaySettingReadDto.toDtos(facility.getWeekdaySettings().getWeekdaySettings()))
        .facilitySchedules(FacilityScheduleReadDto.toDtos(facility.getFacilitySchedules()
            .getFacilitySchedules()))
        .build();
  }
}
