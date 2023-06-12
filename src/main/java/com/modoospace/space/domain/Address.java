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

  private String fullAddress; // 전체 주소
  private String depthFirst; // 시도
  private String depthSecond; // 구
  private String depthThird; // 동

  @Builder
  public Address(String fullAddress, String depthFirst, String depthSecond,
      String depthThird) {
    this.fullAddress = fullAddress;
    this.depthFirst = depthFirst;
    this.depthSecond = depthSecond;
    this.depthThird = depthThird;
  }
}
