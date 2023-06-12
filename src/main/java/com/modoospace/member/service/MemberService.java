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
  public void updateMember(MemberUpdateDto updateDto, String loginEmail) {
    Member admin = findByEmail(loginEmail);
    Member member = findByEmail(updateDto.getEmail());

    member.updateRole(admin, updateDto.getRole());
  }

  private Member findByEmail(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEntityException("사용자"));
    return member;
  }
}
