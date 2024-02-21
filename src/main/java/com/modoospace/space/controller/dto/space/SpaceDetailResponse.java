package com.modoospace.space.controller.dto.space;

import com.modoospace.member.controller.dto.MemberResponse;
import com.modoospace.space.controller.dto.address.AddressResponse;
import com.modoospace.space.controller.dto.category.CategoryResponse;
import com.modoospace.space.controller.dto.facility.FacilityResponse;
import com.modoospace.space.domain.Space;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SpaceDetailResponse {

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

    private List<FacilityResponse> facilities;

    @Builder
    public SpaceDetailResponse(Long id, String name, String description, AddressResponse address,
        MemberResponse host, CategoryResponse category, List<FacilityResponse> facilities) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.host = host;
        this.category = category;
        this.facilities = facilities;
    }

    public static SpaceDetailResponse of(Space space) {
        return SpaceDetailResponse.builder()
            .id(space.getId())
            .name(space.getName())
            .description(space.getDescription())
            .host(MemberResponse.of(space.getHost()))
            .address(AddressResponse.of(space.getAddress()))
            .category(CategoryResponse.of(space.getCategory()))
            .facilities(FacilityResponse.of(space.getFacilities()))
            .build();
    }
}
