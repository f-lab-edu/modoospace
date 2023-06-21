package com.modoospace.member.service;

import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.member.controller.dto.MemberUpdateDto;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

  private final MemberRepository memberRepository;

  @Transactional
  public void updateMemberRole(MemberUpdateDto updateDto, String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    Member member = findMemberByEmail(updateDto.getEmail());

    member.updateRoleOnlyAdmin(updateDto.getRole(), loginMember);
  }

  private Member findMemberByEmail(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEntityException("사용자", email));
    return member;
  }
}
