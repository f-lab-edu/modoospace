package com.modoospace.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.exception.PermissionDeniedException;
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

  @DisplayName("멤버의 Role은 관리자만 변경 가능하다.")
  @Test
  public void updateRoleOnlyAdmin() {
    visitorMember.updateRoleOnlyAdmin(Role.HOST, adminMember);

    visitorMember.verifyRolePermission(Role.HOST);
  }

  @DisplayName("관리자가 아닌 멤버가 Role의 변경을 시도할 경우 예외를 던진다.")
  @Test
  public void updateRoleOnlyAdmin_throwException_ifNotAdmin() {
    assertAll(
        () -> assertThatThrownBy(() -> visitorMember.updateRoleOnlyAdmin(Role.HOST, hostMember))
            .isInstanceOf(PermissionDeniedException.class),
        () -> assertThatThrownBy(() -> visitorMember.updateRoleOnlyAdmin(Role.HOST, visitorMember))
            .isInstanceOf(PermissionDeniedException.class)
    );
  }

  @DisplayName("해당 역할을 가졌는지 권한을 확인한다.")
  @Test
  public void verifyRolePermission() {
    visitorMember.verifyRolePermission(Role.VISITOR);
    hostMember.verifyRolePermission(Role.HOST);
    adminMember.verifyRolePermission(Role.ADMIN);
  }

  @DisplayName("해당 역할을 갖지 않았을 경우 예외를 던진다.")
  @Test
  public void verifyRolePermission_throwException_ifNotSameRole() {
    assertAll(
        () -> assertThatThrownBy(() -> visitorMember.verifyRolePermission(Role.HOST))
            .isInstanceOf(PermissionDeniedException.class),
        () -> assertThatThrownBy(() -> visitorMember.verifyRolePermission(Role.ADMIN))
            .isInstanceOf(PermissionDeniedException.class)
    );
  }
}
