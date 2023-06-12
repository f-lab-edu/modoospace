package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

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
}
