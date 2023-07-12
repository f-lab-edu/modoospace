package com.modoospace.space.controller.dto.facilitySchedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.YearMonth;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FacilitySchedule1MonthDto {

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
  private YearMonth yearMonth;

  public FacilitySchedule1MonthDto(YearMonth yearMonth) {
    this.yearMonth = yearMonth;
  }
}
