package com.modoospace.space.controller.dto;

import javax.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SpaceSearchDto {

  @NotEmpty
  private String depthFirst;

  @NotEmpty
  private String depthSecond;

  @NotEmpty
  private String depthThird;

  @Builder
  public SpaceSearchDto(String depthFirst, String depthSecond, String depthThird) {
    this.depthFirst = depthFirst;
    this.depthSecond = depthSecond;
    this.depthThird = depthThird;
  }
}
