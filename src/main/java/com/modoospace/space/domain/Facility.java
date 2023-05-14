package com.modoospace.space.domain;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Facility {

  @Id
  @GeneratedValue
  private Long facilityId;

  @Column(nullable = false)
  private String name;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "space_id")
  private Space space;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "category_id")
  private Category category;
}
