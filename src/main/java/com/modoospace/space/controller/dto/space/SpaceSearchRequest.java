package com.modoospace.space.controller.dto.space;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpaceSearchRequest {

    private String name;

    private String depthFirst;

    private String depthSecond;

    private String depthThird;

    private Long hostId;

    private Long categoryId;

    @Builder
    public SpaceSearchRequest(String name, String depthFirst, String depthSecond,
        String depthThird, Long hostId, Long categoryId) {
        this.name = name;
        this.depthFirst = depthFirst;
        this.depthSecond = depthSecond;
        this.depthThird = depthThird;
        this.hostId = hostId;
        this.categoryId = categoryId;
    }
}
