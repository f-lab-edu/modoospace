package com.modoospace.space.controller.dto.space;

import com.modoospace.member.controller.dto.MemberReadDto;
import com.modoospace.space.controller.dto.category.CategoryReadDto;
import com.modoospace.space.controller.dto.facility.FacilityReadDto;
import com.modoospace.space.domain.Address;
import com.modoospace.space.domain.Space;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SpaceReadDetailDto {

  @NotNull
  private Long id;

  @NotEmpty
  private String name;

  private String description;

  @NotNull
  private Address address;

  @NotNull
  private MemberReadDto host;

  @NotNull
  private CategoryReadDto category;

  @Builder.Default
  List<FacilityReadDto> facilities = new ArrayList<>();

  public static SpaceReadDetailDto toDto(Space space) {
    return SpaceReadDetailDto.builder()
        .id(space.getId())
        .name(space.getName())
        .description(space.getDescription())
        .address(space.getAddress())
        .host(MemberReadDto.toDto(space.getHost()))
        .category(CategoryReadDto.toDto(space.getCategory()))
        .facilities(FacilityReadDto.toDtos(space.getFacilities()))
        .build();
  }
}
