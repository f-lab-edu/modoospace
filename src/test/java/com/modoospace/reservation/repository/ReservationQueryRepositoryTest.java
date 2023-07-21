package com.modoospace.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.modoospace.TestConfig;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.space.controller.dto.facility.FacilityCreateDto;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.FacilityType;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(TestConfig.class)
public class ReservationQueryRepositoryTest {

  @Autowired
  private ReservationQueryRepository reservationQueryRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private SpaceRepository spaceRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private ReservationRepository reservationRepository;

  private Member visitorMember;

  private Facility roomFacility;

  private LocalDateTime now;

  @BeforeEach
  public void setUp() {
    Member hostMember = Member.builder()
        .email("host@email")
        .name("host")
        .role(Role.HOST)
        .build();
    memberRepository.save(hostMember);

    visitorMember = Member.builder()
        .email("visitor@email")
        .name("visitor")
        .role(Role.VISITOR)
        .build();
    memberRepository.save(visitorMember);

    Category category = Category.builder()
        .name("스터디 공간")
        .build();
    categoryRepository.save(category);

    SpaceCreateUpdateDto spaceCreateDto = SpaceCreateUpdateDto.builder()
        .name("공간이름")
        .description("설명")
        .build();
    Space space = spaceCreateDto.toEntity(category, hostMember);
    spaceRepository.save(space);

    FacilityCreateDto createRoomDto = FacilityCreateDto.builder()
        .name("스터디룸")
        .facilityType(FacilityType.ROOM)
        .description("1~4인실 입니다.")
        .reservationEnable(true)
        .build();
    roomFacility = facilityRepository.save(createRoomDto.toEntity(space));

    now = LocalDateTime.now();
  }

  @Test
  @DisplayName("동일한 시설,시간에 기존 예약이 있다면 true를 반환한다.")
  public void isOverlappingReservation_true() {
    LocalDateTime reservationStart = now;
    LocalDateTime reservationEnd = now.plusHours(3);
    Reservation reservation = Reservation.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
        .visitor(visitorMember)
        .facility(roomFacility)
        .build();
    reservationRepository.save(reservation);

    Boolean isExist = reservationQueryRepository
        .isOverlappingReservation(roomFacility, now, now.plusHours(1));

    assertThat(isExist).isTrue();
  }


  @Test
  @DisplayName("동일한 시설,시간에 기존 예약이 없다면 false를 반환한다.")
  public void isOverlappingReservation_false() {
    Boolean isExist = reservationQueryRepository
        .isOverlappingReservation(roomFacility, now, now.plusHours(1));

    assertThat(isExist).isFalse();
  }
}
