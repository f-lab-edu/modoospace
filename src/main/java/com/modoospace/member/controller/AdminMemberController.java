package com.modoospace.member.controller;

import com.modoospace.config.auth.LoginEmail;
import com.modoospace.member.controller.dto.MemberUpdateRequest;
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

  @PutMapping("/{memberId}")
  public ResponseEntity<Void> updateRole(@PathVariable Long memberId,
      @RequestBody @Valid MemberUpdateRequest updateRequest,
      @LoginEmail String loginEmail) {
    memberService.updateMemberRole(memberId, updateRequest, loginEmail);
    return ResponseEntity.noContent().build();
  }
}
