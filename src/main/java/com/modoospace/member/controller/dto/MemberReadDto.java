package com.modoospace.member.controller.dto;

import com.modoospace.member.domain.Member;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberReadDto {

  @NotNull
  private Long id;

  @NotEmpty
  private String name;

  @NotEmpty
  private String email;

  @Builder
  public MemberReadDto(Long id, String name, String email) {
    this.id = id;
    this.name = name;
    this.email = email;
  }

  public static MemberReadDto toDto(Member member) {
    return MemberReadDto.builder()
        .id(member.getId())
        .name(member.getName())
        .email(member.getEmail())
        .build();
  }
}
