package com.modoospace.space.controller.dto.facilitySchedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilitySchedule;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FacilityScheduleCreateUpdateDto {

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime startDateTime;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime endDateTime;

  @Builder
  public FacilityScheduleCreateUpdateDto(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  public FacilitySchedule toEntity(Facility facility) {
    return FacilitySchedule.builder()
        .startDateTime(startDateTime)
        .endDateTime(endDateTime)
        .facility(facility)
        .build();
  }
}
