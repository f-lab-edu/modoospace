package com.modoospace.space.domain;

import static javax.persistence.FetchType.LAZY;

import com.modoospace.user.domain.User;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Space {

  @Id
  @GeneratedValue
  @Column(name = "space_id")
  private Long id;

  @Column(nullable = false)
  private String name;

  @Embedded
  private Address address;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "host_user_id")
  private User host;

  @OneToMany(mappedBy = "space", cascade = CascadeType.ALL)
  private List<Facility> facilities = new ArrayList<>();
}
