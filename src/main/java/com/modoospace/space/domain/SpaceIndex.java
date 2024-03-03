package com.modoospace.space.domain;

import javax.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "space")
@Getter
@NoArgsConstructor
public class SpaceIndex {

    @Id
    private Long id;
    private String name;
    private String description;
    private String categoryName;
    private String address;

    @Builder
    public SpaceIndex(Long id, String name, String description, String categoryName,
        String address) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryName = categoryName;
        this.address = address;
    }

    public static SpaceIndex of(Space space) {
        return SpaceIndex.builder()
            .id(space.getId())
            .name(space.getName())
            .description(space.getDescription())
            .categoryName(space.getCategory().getName())
            .address(space.getAddress().getDepthFirst()
                + " " + space.getAddress().getDepthSecond()
                + " " + space.getAddress().getDepthThird())
            .build();
    }
}
