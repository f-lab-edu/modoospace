package com.modoospace.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityType;
import com.modoospace.space.domain.Space;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ReservationTest {

  private Member adminMember;
  private Member hostMember;
  private Member visitorMember;

  private Space space;

  private Facility facilityRoom;

  @BeforeEach
  public void setUp() {

    adminMember = Member.builder()
        .email("admin@email")
        .name("admin")
        .role(Role.ADMIN)
        .build();

    hostMember = Member.builder()
        .email("host@email")
        .name("host")
        .role(Role.HOST)
        .build();

    visitorMember = Member.builder()
        .email("visitor@email")
        .name("visitor")
        .role(Role.VISITOR)
        .build();

    space = Space.builder()
        .name("test")
        .host(hostMember)
        .build();

    facilityRoom = Facility.builder()
        .name("룸")
        .facilityType(FacilityType.ROOM)
        .reservationEnable(true)
        .description("설명")
        .space(space).build();
  }

  @DisplayName("방문자는 룸을 예약할 수 있다.")
  @Test
  public void reservationForRoom() {
    Reservation reservation = Reservation.builder()
        .reservationStart(LocalDateTime.now())
        .reservationEnd(LocalDateTime.now().plusHours(3))
        .visitor(visitorMember)
        .status(ReservationStatus.WAITING)
        .facility(facilityRoom)
        .build();

    assertThat(reservation.getVisitor()).isEqualTo(visitorMember);
  }

  @DisplayName("호스트 및 관리자는 예약을 변경할 수 있다.")
  @Test
  public void updateAsHost(){
    Reservation reservation = Reservation.builder()
        .reservationStart(LocalDateTime.now())
        .reservationEnd(LocalDateTime.now().plusHours(3))
        .visitor(visitorMember)
        .status(ReservationStatus.COMPLETED)
        .facility(facilityRoom)
        .build();

    assertAll(
        () -> reservation.updateAsHost(reservation,hostMember),
        () -> reservation.updateAsHost(reservation,adminMember)
    );
  }

  @DisplayName("예약 요청자와 방문자가 다를 경우 예외가 발생한다.")
  @Test
  public void verifySameVisitor(){
    Reservation reservation = Reservation.builder()
        .reservationStart(LocalDateTime.now())
        .reservationEnd(LocalDateTime.now().plusHours(3))
        .visitor(hostMember)
        .status(ReservationStatus.COMPLETED)
        .facility(facilityRoom)
        .build();

    assertThatThrownBy(()->reservation.verifySameVisitor(visitorMember))
        .isInstanceOf(PermissionDeniedException.class);
  }
}
