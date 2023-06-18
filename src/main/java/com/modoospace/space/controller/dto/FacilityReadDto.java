package com.modoospace.space.controller.dto;

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

  private String desc;

  @Builder
  public FacilityReadDto(Long id, String name, FacilityType facilityType, String desc) {
    this.id = id;
    this.name = name;
    this.facilityType = facilityType;
    this.desc = desc;
  }

  public static FacilityReadDto toDto(Facility facility) {
    return FacilityReadDto.builder()
        .id(facility.getId())
        .name(facility.getName())
        .facilityType(facility.getFacilityType())
        .desc(facility.getDesc())
        .build();
  }

  public static List<FacilityReadDto> toDtos(List<Facility> facilities) {
    return facilities.stream()
        .map(FacilityReadDto::toDto)
        .collect(Collectors.toList());
  }
}
