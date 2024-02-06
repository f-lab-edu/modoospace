package com.modoospace.space.controller.dto.category;

import com.modoospace.space.domain.Category;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryResponse {

  @NotNull
  private Long id;

  @NotEmpty
  private String name;

  public CategoryResponse(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public static CategoryResponse of(Category category) {
    return new CategoryResponse(category.getId(), category.getName());
  }
}
