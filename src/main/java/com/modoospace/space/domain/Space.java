package com.modoospace.space.domain;

import static javax.persistence.FetchType.LAZY;

import com.modoospace.common.BaseTimeEntity;
import com.modoospace.exception.HostPermissionException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
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
public class Space extends BaseTimeEntity {

  @Id
  @GeneratedValue
  @Column(name = "space_id")
  private Long id;

  @Column(nullable = false)
  private String name;

  @Embedded
  private Address address;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "host_id")
  private Member host;

  @OneToMany(mappedBy = "space", cascade = CascadeType.ALL)
  private List<Facility> facilities = new ArrayList<>();

  @Builder
  public Space(Long id, String name, Address address, Member host,
      List<Facility> facilities) {
    this.id = id;
    this.name = name;
    this.address = address;
    validateHost(host);
    this.host = host;
    this.facilities = facilities;
  }

  private void validateHost(Member host) {
    if (!host.isRoleEqual(Role.HOST)) {
      throw new HostPermissionException();
    }
  }
}
