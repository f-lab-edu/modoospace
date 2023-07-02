package com.modoospace.space.controller.dto;

import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleCreateDto;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilitySchedule;
import com.modoospace.space.domain.FacilitySchedules;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FacilitySchedulesUpdateDto {

  @NotEmpty
  List<FacilityScheduleCreateDto> facilitySchedules;

  @Builder
  public FacilitySchedulesUpdateDto(List<FacilityScheduleCreateDto> facilitySchedules) {
    this.facilitySchedules = facilitySchedules;
  }

  public Facility toEntity() {
    return Facility.builder()
        .facilitySchedules(new FacilitySchedules(toFacilitySchedules(facilitySchedules)))
        .build();
  }

  private List<FacilitySchedule> toFacilitySchedules(
      List<FacilityScheduleCreateDto> facilitySchedules) {
    return facilitySchedules.stream()
        .map(scheduleCreateDto -> scheduleCreateDto.toEntity())
        .collect(Collectors.toList());
  }
}
