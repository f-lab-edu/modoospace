package com.modoospace.space.controller.dto;

import com.modoospace.space.domain.Address;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SpaceUpdateDto {

  @NotNull
  private Long id;

  @NotEmpty
  private String name;

  @NotNull
  private Address address;

  private Long categoryId;

  @Builder
  public SpaceUpdateDto(Long id, String name, Address address, Long categoryId) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.categoryId = categoryId;
  }
}
