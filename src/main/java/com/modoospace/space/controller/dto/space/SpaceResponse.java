package com.modoospace.space.controller.dto.space;

import com.modoospace.member.controller.dto.MemberResponse;
import com.modoospace.space.controller.dto.address.AddressResponse;
import com.modoospace.space.controller.dto.category.CategoryResponse;
import com.modoospace.space.domain.Space;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SpaceResponse {

    @NotNull
    private Long id;

    @NotEmpty
    private String name;

    private String description;

    @NotNull
    private AddressResponse address;

    @NotNull
    private MemberResponse host;

    @NotNull
    private CategoryResponse category;

    @Builder
    public SpaceResponse(Long id, String name, String description, AddressResponse address,
        MemberResponse host, CategoryResponse category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.host = host;
        this.category = category;
    }

    public static SpaceResponse of(Space space) {
        return SpaceResponse.builder()
            .id(space.getId())
            .name(space.getName())
            .description(space.getDescription())
            .address(AddressResponse.of(space.getAddress()))
            .host(MemberResponse.of(space.getHost()))
            .category(CategoryResponse.of(space.getCategory()))
            .build();
    }
}

