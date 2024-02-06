package com.modoospace.member.controller.dto;

import com.modoospace.member.domain.Role;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateRequest {

  @NotNull
  private Role role;

  @Builder
  public MemberUpdateRequest(@NotNull Role role) {
    this.role = role;
  }
}
