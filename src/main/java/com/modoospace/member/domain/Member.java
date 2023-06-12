package com.modoospace.member.domain;

import com.modoospace.common.BaseTimeEntity;
import com.modoospace.exception.AdminPermissionException;
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

  public Member updateName(String name) {
    this.name = name;
    return this;
  }

  public Member updateRole(Member admin, Role role) {
    if (!admin.isRoleEqual(Role.ADMIN)) {
      throw new AdminPermissionException();
    }

    this.role = role;
    return this;
  }

  public boolean isRoleEqual(Role role) {
    return this.role.equals(role);
  }

  public String getRoleKey() {
    return this.role.getKey();
  }
}
