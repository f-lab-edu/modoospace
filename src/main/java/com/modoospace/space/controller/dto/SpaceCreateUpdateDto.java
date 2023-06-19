package com.modoospace.space.controller.dto;

import com.modoospace.member.domain.Member;
import com.modoospace.space.domain.Address;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.Space;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SpaceCreateUpdateDto {

  @NotEmpty
  private String name;

  private String desc;

  @NotNull
  private Address address;

  @Builder
  public SpaceCreateUpdateDto(String name, String desc, Address address) {
    this.name = name;
    this.desc = desc;
    this.address = address;
  }

  public Space toEntity(Category category, Member host) {
    return Space.builder()
        .name(name)
        .desc(desc)
        .address(address)
        .category(category)
        .host(host)
        .build();
  }
}
