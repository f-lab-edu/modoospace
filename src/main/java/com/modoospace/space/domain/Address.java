package com.modoospace.space.domain;

import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    private String depthFirst; // 시도
    private String depthSecond; // 구
    private String depthThird; // 동
    private String addressNo; // 지번
    private String roadName; // 도로명
    private String buildingNo; // 건물번호
    private String detailAddress; // 나머지 주소
    private String x; // 경도
    private String y; // 위도

    @Builder
    public Address(String depthFirst, String depthSecond, String depthThird, String addressNo,
        String roadName, String buildingNo, String detailAddress, String x, String y) {
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
}
