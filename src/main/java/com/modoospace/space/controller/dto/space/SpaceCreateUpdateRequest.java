package com.modoospace.space.controller.dto.space;

import com.modoospace.member.domain.Member;
import com.modoospace.space.controller.dto.address.AddressCreateUpdateRequest;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.Space;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SpaceCreateUpdateRequest {

    @NotEmpty
    private String name;

    private String description;

    @NotNull
    private AddressCreateUpdateRequest address;

    public SpaceCreateUpdateRequest(String name, String description,
        AddressCreateUpdateRequest address) {
        this.name = name;
        this.description = description;
        this.address = address;
    }

    public Space toEntity(Category category, Member host) {
        return Space.builder()
            .name(name)
            .description(description)
            .address(address.toEntity())
            .category(category)
            .host(host)
            .build();
    }
}
