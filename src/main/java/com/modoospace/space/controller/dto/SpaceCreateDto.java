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
public class SpaceCreateDto {

  @NotEmpty
  private String name;

  @NotNull
  private Address address;

  @NotNull
  private Long categoryId;

  @NotEmpty
  private String hostEmail;

  @Builder
  public SpaceCreateDto(String name, Address address, Long categoryId, String hostEmail) {
    this.name = name;
    this.address = address;
    this.categoryId = categoryId;
    this.hostEmail = hostEmail;
  }

  public Space toEntity(Member host, Category category) {
    return Space.builder()
        .name(name)
        .address(address)
        .category(category)
        .host(host)
        .build();
  }
}
