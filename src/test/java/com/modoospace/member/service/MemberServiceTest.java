package com.modoospace.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.exception.AdminPermissionException;
import com.modoospace.member.controller.dto.MemberUpdateDto;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class MemberServiceTest {

  private MemberService memberService;

  @Autowired
  private MemberRepository memberRepository;

  private Member adminMember;
  private Member hostMember;
  private Member visitorMember;

  @BeforeEach
  public void setUp() {
    memberService = new MemberService(memberRepository);

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

    memberRepository.save(adminMember);
    memberRepository.save(hostMember);
    memberRepository.save(visitorMember);
  }

  @DisplayName("Visitor 사용자를 Host로 변경한디.")
  @Test
  public void updateMember() {
    MemberUpdateDto updateDto = MemberUpdateDto.builder()
        .email("visitor@email")
        .role(Role.HOST)
        .build();

    memberService.updateMember(updateDto, "admin@email");

    Member updateMember = memberRepository.findByEmail("visitor@email").get();
    assertThat(updateMember.isRoleEqual(Role.HOST)).isTrue();
  }

  @DisplayName("관리자가 아닐 경우, 사용자의 권한을 변경할 수 없다. (본인도 포함이다.)")
  @Test
  public void updateMember_throwException_ifNotAdmin() {
    MemberUpdateDto updateDto = MemberUpdateDto.builder()
        .email("visitor@email")
        .role(Role.HOST)
        .build();

    assertAll(
        () -> assertThatThrownBy(() -> memberService.updateMember(updateDto, "visitor@email"))
            .isInstanceOf(AdminPermissionException.class),
        () -> assertThatThrownBy(() -> memberService.updateMember(updateDto, "host@email"))
            .isInstanceOf(AdminPermissionException.class)
    );
  }
}
