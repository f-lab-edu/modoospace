package com.modoospace.space.controller.dto.facility;

import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityType;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FacilityReadDto {

  @NotNull
  private Long id;

  @NotEmpty
  private String name;

  @NotNull
  private FacilityType facilityType;

  @NotNull
  private Boolean reservationEnable;

  private String description;

  @Builder
  public FacilityReadDto(Long id, String name, FacilityType facilityType, Boolean reservationEnable,
      String description) {
    this.id = id;
    this.name = name;
    this.facilityType = facilityType;
    this.reservationEnable = reservationEnable;
    this.description = description;
  }

  public static FacilityReadDto toDto(Facility facility) {
    return FacilityReadDto.builder()
        .id(facility.getId())
        .name(facility.getName())
        .facilityType(facility.getFacilityType())
        .reservationEnable(facility.getReservationEnable())
        .description(facility.getDescription())
        .build();
  }

  public static List<FacilityReadDto> toDtos(List<Facility> facilities) {
    return facilities.stream()
        .map(FacilityReadDto::toDto)
        .collect(Collectors.toList());
  }
}
