package com.modoospace.space.controller.dto;

import com.modoospace.space.domain.Category;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryReadDto {

  @NotNull
  private Long id;

  @NotEmpty
  private String name;

  @Builder
  public CategoryReadDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public static CategoryReadDto toDto(Category category) {
    return CategoryReadDto.builder()
        .id(category.getId())
        .name(category.getName())
        .build();
  }
}
