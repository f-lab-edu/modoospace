package com.modoospace.member.service;

import com.modoospace.global.exception.NotFoundEntityException;
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
  public void updateMemberRole(Long memberId, MemberUpdateDto updateDto, String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    Member member = findMemberById(memberId);

    member.updateRoleOnlyAdmin(updateDto.getRole(), loginMember);
  }

  private Member findMemberByEmail(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEntityException("사용자", email));
    return member;
  }

  private Member findMemberById(Long id) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new NotFoundEntityException("사용자", id));
    return member;
  }
}
