package com.modoospace.member.service;

import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.exception.PermissionDeniedException;
import com.modoospace.member.controller.dto.MemberUpdateDto;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

  private final MemberRepository memberRepository;

  @Transactional
  public void updateMember(MemberUpdateDto updateDto, String loginEmail) {
    Member admin = findByEmail(loginEmail);
    Member member = findByEmail(updateDto.getEmail());

    checkPermission(admin);
    member.updateRole(updateDto.getRole());
  }

  private Member findByEmail(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEntityException("사용자"));
    return member;
  }

  private void checkPermission(Member admin) {
    if (!admin.isRoleEqual(Role.ADMIN)) {
      throw new PermissionDeniedException();
    }
  }
}
