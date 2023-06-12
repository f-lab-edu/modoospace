package com.modoospace.space.controller.dto;

import com.modoospace.member.domain.Member;
import com.modoospace.space.domain.Address;
import com.modoospace.space.domain.Space;
import lombok.Builder;

public class SpaceCreateDto {

  private String name;

  private Address address;

  @Builder
  public SpaceCreateDto(String name, Address address) {
    this.name = name;
    this.address = address;
  }

  public Space toEntity(Member member) {
    return Space.builder()
        .name(name)
        .address(address)
        .host(member)
        .build();
  }
}
