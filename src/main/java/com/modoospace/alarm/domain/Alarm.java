package com.modoospace.alarm.domain;

import com.modoospace.common.BaseTimeEntity;
import com.modoospace.member.domain.Member;
import com.modoospace.reservation.domain.Reservation;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alarm extends BaseTimeEntity {

  @Id
  @GeneratedValue
  @Column(name = "alarm_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reservation_id")
  private Reservation reservation;

  @Enumerated(EnumType.STRING)
  private AlarmType alarmType;

  @Builder
  public Alarm(Long id, Member member, Reservation reservation,
      AlarmType alarmType) {
    this.id = id;
    this.member = member;
    this.reservation = reservation;
    this.alarmType = alarmType;
  }
}
