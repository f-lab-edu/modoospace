package com.modoospace.member.controller.dto;

import com.modoospace.member.domain.Member;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberResponse {

  @NotNull
  private Long id;

  @NotEmpty
  private String name;

  @NotEmpty
  private String email;

  @Builder
  public MemberResponse(Long id, String name, String email) {
    this.id = id;
    this.name = name;
    this.email = email;
  }

  public static MemberResponse of(Member member) {
    return MemberResponse.builder()
        .id(member.getId())
        .name(member.getName())
        .email(member.getEmail())
        .build();
  }
}
