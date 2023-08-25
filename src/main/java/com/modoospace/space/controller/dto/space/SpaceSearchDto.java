package com.modoospace.space.controller.dto.space;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpaceSearchDto {

  private String name;

  private String depthFirst;

  private String depthSecond;

  private String depthThird;

  private Long hostId;

  private Long categoryId;

  public SpaceSearchDto(String name, String depthFirst, String depthSecond,
      String depthThird, Long hostId, Long categoryId) {
    this.name = name;
    this.depthFirst = depthFirst;
    this.depthSecond = depthSecond;
    this.depthThird = depthThird;
    this.hostId = hostId;
    this.categoryId = categoryId;
  }
}
