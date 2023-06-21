package com.modoospace.space.domain;

import static javax.persistence.FetchType.LAZY;

import com.modoospace.common.BaseTimeEntity;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import com.sun.istack.NotNull;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Space extends BaseTimeEntity {

  @Id
  @GeneratedValue
  @Column(name = "space_id")
  private Long id;

  @Column(nullable = false)
  private String name;

  private String description;

  @NotNull
  @Embedded
  private Address address;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "host_id")
  private Member host;

  @Builder.Default
  @OneToMany(mappedBy = "space", cascade = CascadeType.ALL)
  private List<Facility> facilities = new ArrayList<>();

  public Space(Long id, String name, String description, Address address,
      Category category, Member host, List<Facility> facilities) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.address = address;
    this.category = category;

    host.verifyRolePermission(Role.HOST);
    this.host = host;

    this.facilities = facilities;
  }

  public void update(final Space updateSpace) {
    this.name = updateSpace.getName();
    this.description = updateSpace.getDescription();
    this.address = updateSpace.getAddress();
  }

  public void verifyManagementPermission(Member loginMember) {
    if (host == loginMember) {
      return;
    }
    loginMember.verifyRolePermission(Role.ADMIN);
  }
}
