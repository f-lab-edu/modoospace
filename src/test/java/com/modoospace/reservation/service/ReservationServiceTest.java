package com.modoospace.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import com.modoospace.TestConfig;
import com.modoospace.alarm.producer.AlarmProducer;
import com.modoospace.exception.ConflictingReservationException;
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
import com.modoospace.space.repository.FacilityScheduleQueryRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
public class ReservationServiceTest {

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private SpaceRepository spaceRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityScheduleRepository facilityScheduleRepository;

  @Autowired
  private FacilityScheduleQueryRepository facilityScheduleQueryRepository;

  @Autowired
  private ReservationRepository reservationRepository;

  @Autowired
  private ReservationQueryRepository reservationQueryRepository;

  private ReservationService reservationService;

  private Member visitorMember;

  private Member hostMember;

  private Facility roomFacility;

  private Facility seatFacility;

  private LocalDateTime now;

  @BeforeEach
  public void setUp() {
    AlarmProducer alarmProducer = mock(AlarmProducer.class);
    reservationService = new ReservationService(memberRepository, facilityRepository,
        facilityScheduleRepository, facilityScheduleQueryRepository, reservationRepository,
        reservationQueryRepository,
        alarmProducer);

    hostMember = Member.builder()
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

    now = LocalDateTime.now();
  }

  @DisplayName("특정 날짜의 예약가능시간을 조회할 수 있다.")
  @Test
  public void getAvailableTimes() {
    AvailabilityTimeResponseDto retDto = reservationService
        .getAvailabilityTime(roomFacility.getId(), now.toLocalDate());

    // 09:00:00 ~ 23:59:59
    Assertions.assertThat(retDto.getAvailableTimes()).hasSize(15);
  }

  @DisplayName("특정 날짜의 예약가능시간을 조회할 수 있다.(24시간 오픈)")
  @Test
  public void getAvailableTimes_24Open() {
    AvailabilityTimeResponseDto retDto = reservationService
        .getAvailabilityTime(seatFacility.getId(), now.toLocalDate());

    // 00:00:00 ~ 23:59:59
    Assertions.assertThat(retDto.getAvailableTimes()).hasSize(24);
  }

  @DisplayName("특정 날짜의 예약가능시간을 조회할 수 있다.(12~15시 예약존재)")
  @Test
  public void getAvailableTimes_ifPresentReservation() {
    LocalDateTime reservationStart = now.toLocalDate().atTime(LocalTime.of(12, 0, 0));
    LocalDateTime reservationEnd = now.toLocalDate().atTime(LocalTime.of(14, 59, 59));
    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
        .build();
    reservationService.createReservation(dto, roomFacility.getId(), visitorMember.getEmail());

    AvailabilityTimeResponseDto retDto = reservationService
        .getAvailabilityTime(roomFacility.getId(), now.toLocalDate());

    // 09:00:00 ~ 11:59:59, 15:00:00 ~ 23:59:59
    Assertions.assertThat(retDto.getAvailableTimes()).hasSize(15 - 3);
  }

  @DisplayName("로그인한 멤버가 비지터일 경우 예약을 생성할 수 있다.")
  @Test
  public void createReservation_IfVisitor() {
    LocalDateTime reservationStart = now.toLocalDate().atTime(LocalTime.of(12, 0, 0));
    LocalDateTime reservationEnd = now.toLocalDate().atTime(LocalTime.of(14, 59, 59));
    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
        .build();
    Long reservationId = reservationService.createReservation(dto, roomFacility.getId(),
        visitorMember.getEmail());

    ReservationReadDto readDto = reservationService.findReservation(reservationId,
        visitorMember.getEmail());

    assertAll(
        () -> assertThat(readDto.getId()).isEqualTo(reservationId),
        () -> assertThat(readDto.getFacility().getId()).isEqualTo(roomFacility.getId()),
        () -> assertThat(readDto.getMember().getId()).isEqualTo(visitorMember.getId()),
        () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.WAITING)
    );
  }

  @DisplayName("기존 예약과 시간이 겹친다면 Room은 예약할 수 없다.")
  @Test
  public void createReservationRoom_throwException_ifOverlappingReservation() {
    LocalDateTime reservationStart = now.toLocalDate().atTime(LocalTime.of(12, 0, 0));
    LocalDateTime reservationEnd = now.toLocalDate().atTime(LocalTime.of(14, 59, 59));
    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
        .build();
    reservationService.createReservation(dto, roomFacility.getId(), visitorMember.getEmail());

    reservationStart = now.toLocalDate().atTime(LocalTime.of(13, 0, 0));
    reservationEnd = now.toLocalDate().atTime(LocalTime.of(15, 59, 59));
    ReservationCreateDto dto2 = ReservationCreateDto.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
        .build();
    assertThatThrownBy(() -> reservationService
        .createReservation(dto2, roomFacility.getId(), visitorMember.getEmail()))
        .isInstanceOf(ConflictingReservationException.class);
  }

  @DisplayName("좌석을 예약할 경우 예약상태는 완료가 된다.")
  @Test
  public void reservationStatus_IfSeat() {
    LocalDateTime reservationStart = now;
    LocalDateTime reservationEnd = now.plusHours(3);
    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
        .build();
    Long reservationId = reservationService.createReservation(dto, seatFacility.getId(),
        visitorMember.getEmail());

    ReservationReadDto readDto = reservationService.findReservation(reservationId,
        visitorMember.getEmail());
    assertAll(
        () -> assertThat(readDto.getId()).isEqualTo(reservationId),
        () -> assertThat(readDto.getFacility().getId()).isEqualTo(seatFacility.getId()),
        () -> assertThat(readDto.getMember().getId()).isEqualTo(visitorMember.getId()),
        () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.COMPLETED)
    );
  }

  @DisplayName("다른 누군가가 사용중이라면 Seat은 예약할 수 없다.")
  @Test
  public void createReservationSeat_throwException_ifOverlappingReservation() {
    LocalDateTime reservationStart = now;
    LocalDateTime reservationEnd = now.plusHours(3);
    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
        .build();
    reservationService.createReservation(dto, seatFacility.getId(), visitorMember.getEmail());

    reservationStart = now.plusHours(1);
    reservationEnd = now.plusHours(3);
    ReservationCreateDto dto2 = ReservationCreateDto.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
        .build();
    assertThatThrownBy(() -> reservationService
        .createReservation(dto2, seatFacility.getId(), visitorMember.getEmail()))
        .isInstanceOf(ConflictingReservationException.class);
  }

  @Transactional
  @DisplayName("방문자가 본인의 예약을 취소할 수 있다.")
  @Test
  public void cancelReservation() {
    LocalDateTime reservationStart = now.toLocalDate().atTime(LocalTime.of(12, 0, 0));
    LocalDateTime reservationEnd = now.toLocalDate().atTime(LocalTime.of(14, 59, 59));
    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
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
    LocalDateTime reservationStart = now.toLocalDate().atTime(LocalTime.of(12, 0, 0));
    LocalDateTime reservationEnd = now.toLocalDate().atTime(LocalTime.of(14, 59, 59));
    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
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
