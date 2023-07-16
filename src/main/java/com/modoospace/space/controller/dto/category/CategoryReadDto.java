package com.modoospace.space.controller.dto.category;

import com.modoospace.space.domain.Category;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryReadDto {

  @NotNull
  private Long id;

  @NotEmpty
  private String name;

  public CategoryReadDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public static CategoryReadDto toDto(Category category) {
    return new CategoryReadDto(category.getId(), category.getName());
  }
}
