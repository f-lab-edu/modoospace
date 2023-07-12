package com.modoospace.space.controller.dto.facilitySchedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FacilitySchedule1DayDto {

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate date;

  public FacilitySchedule1DayDto(LocalDate date) {
    this.date = date;
  }
}
