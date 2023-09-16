package com.modoospace.member.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.common.exception.PermissionDeniedException;
import com.modoospace.member.controller.dto.MemberUpdateDto;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberServiceTest {

  @Autowired
  private MemberService memberService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private StringRedisTemplate redisTemplate;

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

    memberRepository.save(adminMember);
    memberRepository.save(hostMember);
    memberRepository.save(visitorMember);
  }

  @AfterEach
  public void after() {
    redisTemplate.getConnectionFactory().getConnection().flushAll();
  }

  @DisplayName("Visitor 사용자를 Host로 변경한디.")
  @Test
  public void updateMemberRole() {
    MemberUpdateDto updateDto = MemberUpdateDto.builder()
        .role(Role.HOST)
        .build();

    memberService.updateMemberRole(visitorMember.getId(), updateDto, "admin@email");

    Member updateMember = memberRepository.findByEmail("visitor@email").get();
    updateMember.verifyRolePermission(Role.HOST);
  }

  @DisplayName("관리자가 아닐 경우, 사용자의 권한을 변경할 수 없다. (본인도 포함이다.)")
  @Test
  public void updateMemberRole_throwException_ifNotAdmin() {
    MemberUpdateDto updateDto = MemberUpdateDto.builder()
        .role(Role.HOST)
        .build();

    assertAll(
        () -> assertThatThrownBy(
            () -> memberService.updateMemberRole(visitorMember.getId(), updateDto, "visitor@email"))
            .isInstanceOf(PermissionDeniedException.class),
        () -> assertThatThrownBy(
            () -> memberService.updateMemberRole(hostMember.getId(), updateDto, "host@email"))
            .isInstanceOf(PermissionDeniedException.class)
    );
  }
}
