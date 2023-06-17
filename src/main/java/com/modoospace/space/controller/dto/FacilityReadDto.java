package com.modoospace.space.controller.dto;

import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.Facility;
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
  private Category category;

  @Builder
  public FacilityReadDto(Long id, String name, Category category) {
    this.id = id;
    this.name = name;
    this.category = category;
  }

  public static FacilityReadDto toDto(Facility facility) {
    return FacilityReadDto.builder()
        .id(facility.getId())
        .name(facility.getName())
        .category(facility.getCategory())
        .build();
  }

  public static List<FacilityReadDto> toDtoList(List<Facility> facilities) {
    return facilities.stream()
        .map(FacilityReadDto::toDto)
        .collect(Collectors.toList());
  }
}
