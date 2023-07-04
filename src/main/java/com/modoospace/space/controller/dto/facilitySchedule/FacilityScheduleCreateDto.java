package com.modoospace.space.controller.dto.facilitySchedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modoospace.space.domain.FacilitySchedule;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FacilityScheduleCreateDto {

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime startDateTime;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime endDateTime;

  @Builder
  public FacilityScheduleCreateDto(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  public FacilitySchedule toEntity() {
    return FacilitySchedule.builder()
        .startDateTime(startDateTime)
        .endDateTime(endDateTime)
        .build();
  }
}
