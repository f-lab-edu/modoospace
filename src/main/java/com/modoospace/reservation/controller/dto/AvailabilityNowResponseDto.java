package com.modoospace.reservation.controller.dto;

import com.modoospace.space.controller.dto.facility.FacilityReadDto;
import com.modoospace.space.domain.Facility;
import lombok.Getter;

@Getter
public class AvailabilityNowResponseDto {

  private final FacilityReadDto facility;
  private final Boolean availability;

  public AvailabilityNowResponseDto(FacilityReadDto facilityReadDto,
      Boolean availability) {
    this.facility = facilityReadDto;
    this.availability = availability;
  }

  public static AvailabilityNowResponseDto from(Facility facility,
      Boolean availability) {
    return new AvailabilityNowResponseDto(FacilityReadDto.toDto(facility), availability);
  }
}
