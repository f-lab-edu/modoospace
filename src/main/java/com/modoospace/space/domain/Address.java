package com.modoospace.space.domain;

import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

  private String fullAddress;
  private String depthFirst;
  private String depthSecond;
  private String depthThird;
}
