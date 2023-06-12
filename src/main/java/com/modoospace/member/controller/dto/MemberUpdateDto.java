package com.modoospace.member.controller.dto;

import com.modoospace.member.domain.Role;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateDto {

  @NotEmpty
  private String email;

  @NotNull
  private Role role;

  @Builder
  public MemberUpdateDto(@NotNull String email, @NotNull Role role) {
    this.email = email;
    this.role = role;
  }
}
