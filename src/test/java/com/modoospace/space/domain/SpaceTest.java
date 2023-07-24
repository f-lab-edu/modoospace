package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.global.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SpaceTest {

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

  @DisplayName("호스트만이 공간을 가질 수 있다.")
  @Test
  public void space() {
    Space space = Space.builder()
        .name("test")
        .host(hostMember)
        .build();

    assertThat(space.getHost()).isEqualTo(hostMember);
  }

  @DisplayName("호스트가 아닐 경우 공간을 가질 수 없다.")
  @Test
  public void space_throwException_IfNotHost() {
    assertAll(
        () -> assertThatThrownBy(() -> Space.builder()
            .name("test")
            .host(visitorMember)
            .build()),
        () -> assertThatThrownBy(() -> Space.builder()
            .name("test")
            .host(adminMember)
            .build())
    );
  }

  @DisplayName("공간의 주인/관리자만이 공간 수정/삭제를 할 수 있음을 검증한다.")
  @Test
  public void verifyManagementPermission() {
    Space space = Space.builder()
        .name("test")
        .host(hostMember)
        .build();

    assertAll(
        () -> space.verifyManagementPermission(hostMember),
        () -> space.verifyManagementPermission(adminMember)
    );
  }

  @DisplayName("공간의 주인/관리자가 아닐 경우 공간 수정/삭제 권한 검증 시 예외를 던진다.")
  @Test
  public void verifyManagementPermission_throwException_ifNotPermission() {
    Space space = Space.builder()
        .name("test")
        .host(hostMember)
        .build();

    assertThatThrownBy(() -> space.verifyManagementPermission(visitorMember))
        .isInstanceOf(PermissionDeniedException.class);
  }
}
