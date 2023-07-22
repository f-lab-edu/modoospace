package com.modoospace.reservation.controller.dto;

import com.modoospace.space.controller.dto.facility.FacilityReadDto;
import com.modoospace.space.domain.Facility;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;

@Getter
public class AvailabilityTimeResponseDto {

  private final FacilityReadDto facility;
  private final List<LocalTime> availableTimes;

  public AvailabilityTimeResponseDto(FacilityReadDto facilityReadDto,
      List<LocalTime> availableTimes) {
    this.facility = facilityReadDto;
    this.availableTimes = availableTimes;
  }

  public static AvailabilityTimeResponseDto from(Facility facility,
      List<LocalTime> availableTimes) {
    return new AvailabilityTimeResponseDto(FacilityReadDto.toDto(facility), availableTimes);
  }
}
