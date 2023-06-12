package com.modoospace.member.domain;

import lombok.Getter;

/**
 * 스프링 시큐리티의 권한코드는 ROLE_이 prefix로 붙는것이 관례
 */
@Getter
public enum Role {
  ADMIN("ROLE_ADMIN"),
  HOST("ROLE_HOST"),
  VISITOR("ROLE_VISITOR");

  private String key;

  private Role(String key) {
    this.key = key;
  }
}
