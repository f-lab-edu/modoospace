package com.modoospace.config.auth.dto;

import com.modoospace.member.domain.Member;
import java.io.Serializable;

public class SessionMember implements Serializable {

  private String name;
  private String email;

  public SessionMember(Member member) {
    this.name = member.getName();
    this.email = member.getEmail();
  }
}
