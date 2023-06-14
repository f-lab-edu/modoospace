package com.modoospace.space.controller.dto;

import com.modoospace.member.controller.dto.MemberReadDto;
import com.modoospace.space.domain.Address;
import com.modoospace.space.domain.Space;
import java.time.LocalDateTime;
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

  @NotNull
  private Address address;

  @NotNull
  private MemberReadDto host;

  @Builder.Default
  List<FacilityReadDto> facilities = new ArrayList<>();

  private LocalDateTime createdTime;

  private LocalDateTime updatedTime;

  public static SpaceReadDto toDto(Space space) {
    return SpaceReadDto.builder()
        .id(space.getId())
        .name(space.getName())
        .address(space.getAddress())
        .host(MemberReadDto.toDto(space.getHost()))
        .facilities(FacilityReadDto.toDtoList(space.getFacilities()))
        .createdTime(space.getCreatedTime())
        .updatedTime(space.getUpdatedTime())
        .build();
  }

  public static List<SpaceReadDto> toList(List<Space> spaces) {
    return spaces.stream()
        .map(SpaceReadDto::toDto)
        .collect(Collectors.toList());
  }
}
