package com.modoospace.space.controller.dto;

import com.modoospace.member.domain.Member;
import com.modoospace.space.domain.Address;
import com.modoospace.space.domain.Space;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SpaceCreateDto {

  @NotEmpty
  private String name;

  @NotNull
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
