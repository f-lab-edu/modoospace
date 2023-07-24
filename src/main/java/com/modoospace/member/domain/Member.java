package com.modoospace.member.domain;

import com.modoospace.common.BaseTimeEntity;
import com.modoospace.common.exception.PermissionDeniedException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

  @Id
  @GeneratedValue
  @Column(name = "member_id")
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Builder
  public Member(Long id, String name, String email, Role role) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.role = role;
  }

  public Member updateNameFromProvider(String name) {
    this.name = name;
    return this;
  }

  public GrantedAuthority createGrantedAuthority() {
    return new SimpleGrantedAuthority(role.getKey());
  }

  public void updateRoleOnlyAdmin(Role role, Member loginMember) {
    loginMember.verifyRolePermission(Role.ADMIN);
    this.role = role;
  }

  public void verifyRolePermission(Role role) {
    if (!isSameRole(role)) {
      throw new PermissionDeniedException();
    }
  }

  public boolean isSameRole(Role role) {
    return this.role.equals(role);
  }
}
