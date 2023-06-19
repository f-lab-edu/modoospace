package com.modoospace.space.controller.dto;

import com.modoospace.member.controller.dto.MemberReadDto;
import com.modoospace.space.domain.Address;
import com.modoospace.space.domain.Space;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
public class SpaceReadDto {

  @NotNull
  private Long id;

  @NotEmpty
  private String name;

  private String desc;

  @NotNull
  private Address address;

  @NotNull
  private MemberReadDto host;

  @NotNull
  private CategoryReadDto category;

  @Builder.Default
  List<FacilityReadDto> facilities = new ArrayList<>();

  public static SpaceReadDto toDto(Space space) {
    return SpaceReadDto.builder()
        .id(space.getId())
        .name(space.getName())
        .desc(space.getDesc())
        .address(space.getAddress())
        .host(MemberReadDto.toDto(space.getHost()))
        .category(CategoryReadDto.toDto(space.getCategory()))
        .facilities(FacilityReadDto.toDtos(space.getFacilities()))
        .build();
  }

  public static List<SpaceReadDto> toDtos(List<Space> spaces) {
    return spaces.stream()
        .map(SpaceReadDto::toDto)
        .collect(Collectors.toList());
  }
}
