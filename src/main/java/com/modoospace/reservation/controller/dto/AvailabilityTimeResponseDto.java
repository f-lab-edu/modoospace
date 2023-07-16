package com.modoospace.reservation.controller.dto;

import java.time.LocalTime;
import java.util.List;
import lombok.Getter;

@Getter
public class AvailabilityTimeResponseDto {
  private final Long id;
  private final List<LocalTime> availableTimes;

  public AvailabilityTimeResponseDto(Long id, List<LocalTime> availableTimes) {
    this.id = id;
    this.availableTimes = availableTimes;
  }

  public static AvailabilityTimeResponseDto from(Long id, List<LocalTime> availableTimes) {
    return new AvailabilityTimeResponseDto(id, availableTimes);
  }
}
