package com.modoospace.space.controller.dto.address;

import com.modoospace.space.domain.Address;
import javax.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddressCreateUpdateRequest {

    @NotEmpty
    private String depthFirst; // 시도

    @NotEmpty
    private String depthSecond; // 구

    @NotEmpty
    private String depthThird; // 동

    @NotEmpty
    private String addressNo; // 지번

    @NotEmpty
    private String roadName; // 도로명

    @NotEmpty
    private String buildingNo; // 건물번호

    private String detailAddress; // 나머지 주소

    private String x; // 경도

    private String y; // 위도

    @Builder
    public AddressCreateUpdateRequest(String depthFirst, String depthSecond, String depthThird,
        String addressNo, String roadName, String buildingNo, String detailAddress, String x,
        String y) {
        this.depthFirst = depthFirst;
        this.depthSecond = depthSecond;
        this.depthThird = depthThird;
        this.addressNo = addressNo;
        this.roadName = roadName;
        this.buildingNo = buildingNo;
        this.detailAddress = detailAddress;
        this.x = x;
        this.y = y;
    }

    public Address toEntity() {
        return Address.builder()
            .depthFirst(depthFirst)
            .depthSecond(depthSecond)
            .depthThird(depthThird)
            .addressNo(addressNo)
            .roadName(roadName)
            .buildingNo(buildingNo)
            .detailAddress(detailAddress)
            .x(x)
            .y(y)
            .build();
    }
}
