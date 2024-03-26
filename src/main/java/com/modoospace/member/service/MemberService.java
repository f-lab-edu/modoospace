package com.modoospace.member.service;

import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.member.controller.dto.MemberUpdateRequest;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.repository.MemberCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

  private final MemberRepository memberRepository;
  private final MemberCacheRepository memberCacheRepository;

  @Transactional
  public void updateMemberRole(Long memberId, MemberUpdateRequest updateRequest, Member loginMember) {
    Member member = findMemberById(memberId);

    member.updateRoleOnlyAdmin(updateRequest.getRole(), loginMember);
    memberCacheRepository.save(member);
  }

  public Member findMemberByEmail(String email) {
    return memberCacheRepository.findByEmail(email)
        .orElseGet(
            () -> memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundEntityException("사용자", email))
        );
  }

  public Member findMemberById(Long id) {
      return memberRepository.findById(id)
        .orElseThrow(() -> new NotFoundEntityException("사용자", id));
  }
}
