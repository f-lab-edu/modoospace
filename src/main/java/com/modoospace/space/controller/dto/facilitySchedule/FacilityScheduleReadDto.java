package com.modoospace.space.controller.dto.facilitySchedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modoospace.space.domain.FacilitySchedule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FacilityScheduleReadDto {

  @NotNull
  private Long id;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime startDateTime;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime endDateTime;

  @Builder
  public FacilityScheduleReadDto(Long id, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    this.id = id;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  public static FacilityScheduleReadDto toDto(FacilitySchedule facilitySchedule) {
    return FacilityScheduleReadDto.builder()
        .id(facilitySchedule.getId())
        .startDateTime(facilitySchedule.getStartDateTime())
        .endDateTime(facilitySchedule.getEndDateTime())
        .build();
  }

  public static List<FacilityScheduleReadDto> toDtos(List<FacilitySchedule> facilitySchedules) {
    return facilitySchedules.stream()
        .map(FacilityScheduleReadDto::toDto)
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "FacilityScheduleReadDto{" +
        "id=" + id +
        ", startDateTime=" + startDateTime +
        ", endDateTime=" + endDateTime +
        '}';
  }
}
