package com.modoospace.member.controller;

import com.modoospace.config.auth.LoginEmail;
import com.modoospace.member.controller.dto.MemberUpdateDto;
import com.modoospace.member.service.MemberService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class MemberController {

  private final MemberService memberService;

  @PutMapping("/member")
  public ResponseEntity<Void> updateMemberRole(@RequestBody @Valid MemberUpdateDto updateDto,
      @LoginEmail String loginEmail) {
    memberService.updateMemberRole(updateDto, loginEmail);
    return ResponseEntity.noContent().build();
  }
}
