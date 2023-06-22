package com.modoospace.space.controller.dto;

import com.modoospace.member.controller.dto.MemberReadDto;
import com.modoospace.space.domain.Address;
import com.modoospace.space.domain.Space;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SpaceReadDto {

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

  @Builder
  public SpaceReadDto(Long id, String name, String description, Address address, MemberReadDto host,
      CategoryReadDto category) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.address = address;
    this.host = host;
    this.category = category;
  }

  public static SpaceReadDto toDto(Space space) {
    return SpaceReadDto.builder()
        .id(space.getId())
        .name(space.getName())
        .description(space.getDescription())
        .address(space.getAddress())
        .host(MemberReadDto.toDto(space.getHost()))
        .category(CategoryReadDto.toDto(space.getCategory()))
        .build();
  }

  public static List<SpaceReadDto> toDtos(List<Space> spaces) {
    return spaces.stream()
        .map(SpaceReadDto::toDto)
        .collect(Collectors.toList());
  }
}

