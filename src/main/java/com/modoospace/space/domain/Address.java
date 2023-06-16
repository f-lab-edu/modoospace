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
  private String detailAddress; // 상세 주소

  @Builder
  public Address(String depthFirst, String depthSecond, String depthThird, String detailAddress) {
    this.depthFirst = depthFirst;
    this.depthSecond = depthSecond;
    this.depthThird = depthThird;
    this.detailAddress = detailAddress;
  }
}
