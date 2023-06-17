package com.modoospace.space.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

  @Id
  @GeneratedValue
  @Column(name = "category_id")
  private Long id;

  @Column(nullable = false)
  private String name;

  @Builder
  public Category(Long id, String name) {
    this.id = id;
    this.name = name;
  }
}
