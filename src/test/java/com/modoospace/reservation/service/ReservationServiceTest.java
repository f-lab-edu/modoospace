package com.modoospace.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.reservation.controller.dto.ReservationCreateDto;
import com.modoospace.reservation.controller.dto.ReservationReadDto;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.reservation.serivce.ReservationService;
import com.modoospace.space.controller.dto.SpaceCreateUpdateDto;
import com.modoospace.space.domain.Address;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.FacilityType;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import com.modoospace.space.sevice.SpaceService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class ReservationServiceTest {

  private ReservationService reservationService;

  @Autowired
  private ReservationRepository reservationRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private SpaceRepository spaceRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  private Member visitorMember;
  private Member hostMember;
  private Facility seat;
  private Facility room;

  private Address address;
  private Category category;
  private ReservationCreateDto reservationCreateDto;

  @BeforeEach
  public void setUp() {
    createMembers();
    createAddress();
    createCategory();
    createFacilities();
    reservationService = new ReservationService(reservationRepository, facilityRepository, memberRepository);
    reservationCreateDto = createReservationDto(LocalDateTime.now(), LocalDateTime.now().plusHours(3));
  }

  @DisplayName("로그인한 멤버가 비지터일 경우 예약을 생성할 수 있다.")
  @Test
  public void createReservation_IfVisitor() {
    Reservation reservation = reservationService.createReservation(reservationCreateDto,
        room.getId(),
        visitorMember.getEmail());

    ReservationReadDto readDto = reservationService.findReservation(reservation.getId());

    assertAll(
        () -> assertThat(readDto.getId()).isEqualTo(reservation.getId()),
        () -> assertThat(readDto.getFacility()).isEqualTo(reservation.getFacility()),
        () -> assertThat(readDto.getVisitor()).isEqualTo(reservation.getVisitor()),
        () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.WAITING)
    );
  }

  @DisplayName("좌석을 예약할 경우 예약상태는 완료가 된다.")
  @Test
  public void reservationStatus_IfSeat() {
    Reservation reservation = reservationService.createReservation(reservationCreateDto,
        seat.getId(),
        visitorMember.getEmail());

    ReservationReadDto readDto = reservationService.findReservation(reservation.getId());
    assertAll(
        () -> assertThat(readDto.getId()).isEqualTo(reservation.getId()),
        () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.COMPLETED)
    );
  }

  @DisplayName("방문자가 본인의 예약을 취소할 수 있다.")
  @Test
  public void cancelReservation() {
    Reservation reservation = reservationService.createReservation(reservationCreateDto,
        seat.getId(),
        visitorMember.getEmail());

    reservationService.cancelReservation(reservation.getId(), visitorMember.getEmail());

    ReservationReadDto readDto = reservationService.findReservation(reservation.getId());

    assertAll(
        () -> assertThat(readDto.getId()).isEqualTo(reservation.getId()),
        () -> assertThat(readDto.getVisitor()).isEqualTo(visitorMember),
        () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.CANCELED)
    );
  }

  @DisplayName("예약을 생성한 사용자가 아닌 다른 사용자가 해당 예약을 취소하려고 한다면 예외가 발생한다")
  @Test
  public void cancelReservation_throwException_IfNotMyReservation() {
    Reservation reservation = reservationService.createReservation(reservationCreateDto,
        seat.getId(),
        visitorMember.getEmail());

    assertAll(
        () -> assertThatThrownBy(
            () -> reservationService.cancelReservation(reservation.getId(), hostMember.getEmail()))
            .isInstanceOf(PermissionDeniedException.class)
    );
  }


  private void createFacilities() {
    SpaceService spaceService = new SpaceService(memberRepository, spaceRepository, categoryRepository);
    SpaceCreateUpdateDto createDto = createSpaceDto(address);
    Long spaceId = spaceService.createSpace(category.getId(), createDto, hostMember.getEmail());
    Space space = spaceService.findSpaceById(spaceId);
    room = createFacility(FacilityType.ROOM, "스터디 룸", space, "facilityRoom");
    seat = createFacility(FacilityType.SEAT, "좌석", space, "facilitySeat");
  }

  private void createMembers() {
    hostMember = createMember("host", Role.HOST);
    visitorMember = createMember("visitor", Role.VISITOR);
  }

  private Member createMember(String name, Role role) {
    Member member = Member.builder()
        .email(name + "@email")
        .name(name)
        .role(role)
        .build();
    return memberRepository.save(member);
  }

  private void createAddress() {
    address = Address.builder()
        .depthFirst("depthFirst")
        .depthSecond("depthSecond")
        .depthThird("depthThird")
        .detailAddress("detailAddress")
        .build();
  }

  private void createCategory() {
    Category build = Category.builder()
        .name("스터디 공간")
        .build();
    category = categoryRepository.save(build);
  }

  private SpaceCreateUpdateDto createSpaceDto(Address address) {
    return SpaceCreateUpdateDto.builder()
        .name("공간이름")
        .description("설명")
        .address(address)
        .build();
  }

  private Facility createFacility(FacilityType facilityType,
      String description, Space space, String name) {
    Facility facility = Facility.builder()
        .facilityType(facilityType)
        .reservationEnable(true)
        .description(description)
        .space(space)
        .name(name)
        .build();
    return facilityRepository.save(facility);
  }

  private ReservationCreateDto createReservationDto(LocalDateTime reservationStart,
      LocalDateTime reservationEnd) {
    return ReservationCreateDto.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
        .build();
  }

}
