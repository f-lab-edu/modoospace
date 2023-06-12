package com.modoospace.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

  private Member adminMember;
  private Member hostMember;
  private Member visitorMember;

  @BeforeEach
  public void setUp() {
    adminMember = Member.builder()
        .email("admin@email")
        .name("admin")
        .role(Role.ADMIN)
        .build();

    hostMember = Member.builder()
        .email("host@email")
        .name("host")
        .role(Role.HOST)
        .build();

    visitorMember = Member.builder()
        .email("visitor@email")
        .name("visitor")
        .role(Role.VISITOR)
        .build();
  }

  @DisplayName("관리자일 경우 해당 멤버의 권한을 변경할 수 있다.")
  @Test
  public void updateRole() {
    assertAll(
        () -> visitorMember.updateRole(adminMember, Role.HOST),
        () -> assertThat(visitorMember.isRoleEqual(Role.HOST)).isTrue(),
        () -> visitorMember.updateRole(adminMember, Role.ADMIN),
        () -> assertThat(visitorMember.isRoleEqual(Role.ADMIN)).isTrue()
    );
  }

  @DisplayName("멤버 권한 변경 시 관리자가 아닐 경우 예외를 던진다.")
  @Test
  public void updateRole_throwException_IfNotAdmin() {
    assertAll(
        () -> assertThatThrownBy(() -> visitorMember.updateRole(visitorMember, Role.HOST)),
        () -> assertThatThrownBy(() -> visitorMember.updateRole(hostMember, Role.HOST))
    );
  }
}
