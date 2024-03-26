package com.modoospace.member.controller;

import com.modoospace.config.auth.aop.CheckLogin;
import com.modoospace.config.auth.resolver.LoginMember;
import com.modoospace.member.controller.dto.MemberUpdateRequest;
import com.modoospace.member.domain.Member;
import com.modoospace.member.service.MemberService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/members")
public class AdminMemberController {

  private final MemberService memberService;

  @CheckLogin
  @PutMapping("/{memberId}")
  public ResponseEntity<Void> updateRole(@PathVariable Long memberId,
      @RequestBody @Valid MemberUpdateRequest updateRequest,
      @LoginMember Member member) {
    memberService.updateMemberRole(memberId, updateRequest, member);
    return ResponseEntity.noContent().build();
  }
}
