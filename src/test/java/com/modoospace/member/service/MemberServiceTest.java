package com.modoospace.member.service;

import com.modoospace.AbstractIntegrationContainerBaseTest;
import com.modoospace.common.exception.PermissionDeniedException;
import com.modoospace.member.controller.dto.MemberUpdateRequest;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
class MemberServiceTest extends AbstractIntegrationContainerBaseTest {

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

        adminMember = memberRepository.save(adminMember);
        hostMember = memberRepository.save(hostMember);
        visitorMember = memberRepository.save(visitorMember);
    }

    @AfterEach
    public void after() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    @DisplayName("Visitor 사용자를 Host로 변경한디.")
    @Test
    public void updateMemberRole() {
        MemberUpdateRequest updateRequest = MemberUpdateRequest.builder()
                .role(Role.HOST)
                .build();

        memberService.updateMemberRole(visitorMember.getId(), updateRequest, adminMember);

        Member updateMember = memberRepository.findByEmail("visitor@email").get();
        updateMember.verifyRolePermission(Role.HOST);
    }

    @DisplayName("관리자가 아닐 경우, 사용자의 권한을 변경할 수 없다. (본인도 포함이다.)")
    @Test
    public void updateMemberRole_throwException_ifNotAdmin() {
        MemberUpdateRequest updateRequest = MemberUpdateRequest.builder()
                .role(Role.HOST)
                .build();

        assertAll(
                () -> assertThatThrownBy(
                        () -> memberService.updateMemberRole(visitorMember.getId(), updateRequest, visitorMember))
                        .isInstanceOf(PermissionDeniedException.class),
                () -> assertThatThrownBy(
                        () -> memberService.updateMemberRole(hostMember.getId(), updateRequest, hostMember))
                        .isInstanceOf(PermissionDeniedException.class)
        );
    }
}
