package com.modoospace.space.controller.dto.address;

import com.modoospace.space.domain.Address;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddressResponse {

    private String depthFirst; // 시도
    private String depthSecond; // 구
    private String depthThird; // 동
    private String noAddress; // 지번주소
    private String roadAddress; // 도로명주소

    @Builder
    public AddressResponse(String depthFirst, String depthSecond, String depthThird,
        String noAddress,
        String roadAddress) {
        this.depthFirst = depthFirst;
        this.depthSecond = depthSecond;
        this.depthThird = depthThird;
        this.noAddress = noAddress;
        this.roadAddress = roadAddress;
    }

    public static AddressResponse of(Address address) {
        return AddressResponse.builder()
            .depthFirst(address.getDepthFirst())
            .depthSecond(address.getDepthSecond())
            .depthThird(address.getDepthThird())
            .noAddress(getNoAddress(address))
            .noAddress(getRoadAddress(address))
            .build();
    }

    private static String getNoAddress(Address address) {
        return address.getDepthFirst() + " " + address.getDepthSecond() + " "
            + address.getDepthThird() + " " + address.getAddressNo() + " "
            + address.getDetailAddress();
    }

    private static String getRoadAddress(Address address) {
        return address.getDepthFirst() + " " + address.getDepthSecond() + " "
            + address.getRoadName() + " " + address.getBuildingNo() + " "
            + address.getDetailAddress();
    }
}
