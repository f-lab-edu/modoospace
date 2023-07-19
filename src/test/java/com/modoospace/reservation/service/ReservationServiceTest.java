package com.modoospace.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.TestConfig;
import com.modoospace.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.reservation.controller.dto.AvailabilityTimeResponseDto;
import com.modoospace.reservation.controller.dto.ReservationCreateDto;
import com.modoospace.reservation.controller.dto.ReservationReadDto;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.reservation.serivce.ReservationService;
import com.modoospace.space.controller.dto.facility.FacilityCreateDto;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateDto;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.FacilityScheduleRepository;
import com.modoospace.space.domain.FacilityType;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import com.modoospace.space.sevice.FacilityScheduleService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Import({TestConfig.class})
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ReservationServiceTest {

  @Autowired
  private ReservationRepository reservationRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityScheduleRepository facilityScheduleRepository;

  @Autowired
  private ReservationQueryRepository reservationQueryRepository;

  @Autowired
  private SpaceRepository spaceRepository;

  private ReservationService reservationService;

  private FacilityScheduleService facilityScheduleService;

  private Member visitorMember;

  private Member hostMember;

  private Facility roomFacility;

  private Facility seatFacility;

  @BeforeEach
  public void setUp() {
    facilityScheduleService = new FacilityScheduleService(facilityScheduleRepository,
        facilityRepository, memberRepository);

    reservationService = new ReservationService(reservationRepository, facilityRepository,
        memberRepository, reservationQueryRepository, facilityScheduleService);

    visitorMember = Member.builder()
        .email("visitor@email")
        .name("visitor")
        .role(Role.VISITOR)
        .build();
    memberRepository.save(visitorMember);

    hostMember = Member.builder()
        .email("host@email")
        .name("host")
        .role(Role.HOST)
        .build();
    memberRepository.save(hostMember);

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
        .timeSettings(Arrays
            .asList(new TimeSettingCreateDto(LocalTime.of(9, 0, 0), LocalTime.of(23, 59, 59))))
        .build();
    roomFacility = facilityRepository.save(createRoomDto.toEntity(space));

    FacilityCreateDto createSeatDto = FacilityCreateDto.builder()
        .name("스터디좌석")
        .facilityType(FacilityType.SEAT)
        .description("개인좌석입니다.")
        .reservationEnable(true)
        .timeSettings(Arrays
            .asList(new TimeSettingCreateDto(LocalTime.of(0, 0, 0), LocalTime.of(23, 59, 59))))
        .build();
    seatFacility = facilityRepository.save(createSeatDto.toEntity(space));
  }

  @DisplayName("특정 날짜의 예약가능시간을 조회할 수 있다.")
  @Test
  public void getAvailableTimes() {
    LocalDate requestDate = LocalDate.now();
    AvailabilityTimeResponseDto retDto = reservationService
        .getAvailabilityTime(roomFacility.getId(), requestDate);

    // 09:00:00 ~ 23:59:59
    Assertions.assertThat(retDto.getAvailableTimes()).hasSize(15);
  }

  @DisplayName("특정 날짜의 예약가능시간을 조회할 수 있다.(24시간 오픈)")
  @Test
  public void getAvailableTimes_24Open() {
    LocalDate requestDate = LocalDate.now();
    AvailabilityTimeResponseDto retDto = reservationService
        .getAvailabilityTime(seatFacility.getId(), requestDate);

    // 00:00:00 ~ 23:59:59
    Assertions.assertThat(retDto.getAvailableTimes()).hasSize(24);
  }

  @DisplayName("로그인한 멤버가 비지터일 경우 예약을 생성할 수 있다.")
  @Test
  public void createReservation_IfVisitor() {
    LocalDate requestDate = LocalDate.now();
    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(requestDate.atTime(LocalTime.of(12, 0, 0)))
        .reservationEnd(requestDate.atTime(LocalTime.of(14, 59, 0)))
        .build();
    Long reservationId = reservationService.createReservation(dto, roomFacility.getId(),
        visitorMember.getEmail());

    ReservationReadDto readDto = reservationService.findReservation(reservationId,
        visitorMember.getEmail());

    assertAll(
        () -> assertThat(readDto.getId()).isEqualTo(reservationId),
        () -> assertThat(readDto.getFacility().getId()).isEqualTo(
            readDto.getFacility().getId()),
        () -> assertThat(readDto.getMember().getId()).isEqualTo(visitorMember.getId()),
        () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.WAITING)
    );
  }

  @DisplayName("좌석을 예약할 경우 예약상태는 완료가 된다.")
  @Test
  public void reservationStatus_IfSeat() {
    LocalDate requestDate = LocalDate.now();
    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(requestDate.atTime(LocalTime.of(12, 0, 0)))
        .reservationEnd(requestDate.atTime(LocalTime.of(14, 59, 0)))
        .build();
    Long reservationId = reservationService.createReservation(dto, seatFacility.getId(),
        visitorMember.getEmail());

    ReservationReadDto readDto = reservationService.findReservation(reservationId,
        visitorMember.getEmail());
    assertAll(
        () -> assertThat(readDto.getId()).isEqualTo(reservationId),
        () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.COMPLETED)
    );
  }

  @Transactional
  @DisplayName("방문자가 본인의 예약을 취소할 수 있다.")
  @Test
  public void cancelReservation() {
    LocalDate requestDate = LocalDate.now();
    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(requestDate.atTime(LocalTime.of(12, 0, 0)))
        .reservationEnd(requestDate.atTime(LocalTime.of(14, 59, 0)))
        .build();
    Long reservationId = reservationService
        .createReservation(dto, roomFacility.getId(), visitorMember.getEmail());
    reservationService.cancelReservation(reservationId, visitorMember.getEmail());

    ReservationReadDto readDto = reservationService
        .findReservation(reservationId, visitorMember.getEmail());

    assertAll(
        () -> assertThat(readDto.getId()).isEqualTo(reservationId),
        () -> assertThat(readDto.getMember().getEmail()).isEqualTo(visitorMember.getEmail()),
        () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.CANCELED)
    );
  }

  @DisplayName("예약을 생성한 사용자가 아닌 다른 사용자가 해당 예약을 취소하려고 한다면 예외가 발생한다")
  @Test
  public void cancelReservation_throwException_IfNotMyReservation() {
    LocalDate requestDate = LocalDate.now();
    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(requestDate.atTime(LocalTime.of(12, 0, 0)))
        .reservationEnd(requestDate.atTime(LocalTime.of(14, 59, 0)))
        .build();
    Long reservationId = reservationService
        .createReservation(dto, roomFacility.getId(), visitorMember.getEmail());

    assertAll(
        () -> assertThatThrownBy(
            () -> reservationService.cancelReservation(reservationId, hostMember.getEmail()))
            .isInstanceOf(PermissionDeniedException.class)
    );
  }
}
