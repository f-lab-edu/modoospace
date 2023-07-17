package com.modoospace.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.TestConfig;
import com.modoospace.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.reservation.controller.dto.AvailabilityTimeRequestDto;
import com.modoospace.reservation.controller.dto.AvailabilityTimeResponseDto;
import com.modoospace.reservation.controller.dto.ReservationCreateDto;
import com.modoospace.reservation.controller.dto.ReservationReadDto;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.reservation.serivce.ReservationService;
import com.modoospace.space.controller.dto.facility.FacilityCreateDto;
import com.modoospace.space.controller.dto.facility.FacilityReadDetailDto;
import com.modoospace.space.controller.dto.facility.FacilityReadDto;
import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleReadDto;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateDto;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.FacilityType;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import com.modoospace.space.sevice.FacilityService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
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
  private ReservationQueryRepository reservationQueryRepository;

  @Autowired
  private SpaceRepository spaceRepository;

  private FacilityService facilityService;

  private ReservationService reservationService;

  private Member visitorMember;

  private Member hostMember;

  private Long facilityRoomId;

  private Space space;

  @BeforeEach
  public void setUp() {
    facilityService = new FacilityService(memberRepository, spaceRepository, facilityRepository);

    reservationService = new ReservationService(reservationRepository, facilityRepository,
        memberRepository, reservationQueryRepository, facilityService);

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
    space = spaceCreateDto.toEntity(category, hostMember);
    spaceRepository.save(space);

    FacilityCreateDto createDto = FacilityCreateDto.builder()
        .name("스터디룸1")
        .facilityType(FacilityType.ROOM)
        .description("1~4인실 입니다.")
        .reservationEnable(true)
        .timeSettings(Arrays
            .asList(new TimeSettingCreateDto(LocalTime.of(9, 0, 0), LocalTime.of(23, 59, 59))))
        .build();
    facilityRoomId = facilityService
        .createFacility(space.getId(), createDto, hostMember.getEmail());
  }

  @DisplayName("방문자는 선택한 시설과 날짜에 대한 예약 가능 여부를 확인할 수 있다.")
  @Test
  public void checkAvailableDate() {
    FacilityReadDetailDto facility = facilityService.findFacility(facilityRoomId);
    List<FacilityScheduleReadDto> schedules = facility.getFacilitySchedules();

    LocalDate requestDate = LocalDate.of(2023, 7, 3);

    boolean hasMatchingDate = schedules.stream()
        .anyMatch(schedule -> schedule.getStartDateTime().toLocalDate().equals(requestDate));

    assertThat(hasMatchingDate).isTrue();
  }

  @DisplayName("visitor는 시설의 예약 가능한 시간을 시간 단위(HH:mm:ss)로 조회할 수 있다.")
  @Test
  void testGetAvailability() {
    LocalDate requestDate = LocalDate.of(2023, 7, 3);
    FacilityReadDetailDto facilityReadDetailDto = facilityService.findFacility(facilityRoomId);
    Facility facility = facilityRepository.findById(facilityRoomId).get();
    FacilityReadDto facilityReadDto = FacilityReadDto.toDto(facility);

    //시설의 미래 3개월치 스케줄 조회
    List<FacilityScheduleReadDto> facilitySchedules = facilityReadDetailDto.getFacilitySchedules();

    // 요청된 날짜에 대한 가능한 시간 가져오기
    List<LocalTime> availableTimes = reservationService.getAvailableTimes(requestDate,
        facilitySchedules);

    // 요청된 날짜에 대한 예약된 시간 가져오기
    List<LocalTime> reservedTimes = reservationService.getReservedTimes(requestDate, facilityRoomId);

    // 예약된 시간을 제외한 가능한 시간 필터링
    List<LocalTime> availableTimesWithoutReservation = availableTimes.stream()
        .filter(time -> !reservedTimes.contains(time))
        .collect(Collectors.toList());
    AvailabilityTimeResponseDto expectedResponse = new AvailabilityTimeResponseDto(facilityReadDto,
        availableTimesWithoutReservation);

    // 결과
    AvailabilityTimeRequestDto requestDto = new AvailabilityTimeRequestDto(facilityRoomId, requestDate);
    AvailabilityTimeResponseDto response = reservationService.getAvailabilityTime(facilityRoomId,
        requestDto);

    // 결과 검증
    assertThat(response.getFacility().getId()).isEqualTo(expectedResponse.getFacility().getId());
    assertThat(expectedResponse.getAvailableTimes()).isEqualTo(response.getAvailableTimes());
  }

  @DisplayName("특정 예약일에 예약상태가 완료, 대기중인 예약을 조회할 수 있다.")
  @Test
  public void findActiveReservations() {
    // Given
    Long facilityId = facilityRoomId;
    LocalDate requestDate = LocalDate.of(2023, 7, 1);

    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(requestDate.atTime(LocalTime.of(12, 0, 0)))
        .reservationEnd(requestDate.atTime(LocalTime.of(14, 59, 0)))
        .build();
    reservationService.createReservation(dto, facilityId, visitorMember.getEmail());

    LocalDateTime startDateTime = requestDate.atStartOfDay();
    LocalDateTime endDateTime = requestDate.atTime(LocalTime.MAX);
    List<ReservationStatus> activeStatuses = ReservationStatus.getActiveStatuses();

    // When
    List<Reservation> activeReservations = reservationRepository.findByReservationStartBetweenAndStatusInAndFacility_Id(
        startDateTime,
        endDateTime,
        activeStatuses,
        facilityId
    );

    Assertions.assertThat(activeReservations).isNotEmpty();
    activeReservations.forEach(reservation -> {
      ReservationStatus status = reservation.getStatus();
      Assertions.assertThat(status).isIn(ReservationStatus.COMPLETED, ReservationStatus.WAITING);
    });
  }

  @DisplayName("특정 날짜의 예약가능시간을 조회할 수 있다.")
  @Test
  public void getAvailableTimes() {
    // 시설 선택
    Long facilityId = facilityRoomId;
    FacilityReadDetailDto facility = facilityService.findFacility(facilityId);
    List<FacilityScheduleReadDto> schedules = facility.getFacilitySchedules();

    // 유저가 조회한 날짜
    LocalDate requestDate = LocalDate.of(2023, 7, 2);

    // 예약 가능한 시간들
    List<LocalTime> availableTimes = schedules.stream()
        .filter(schedule -> isMatchingDate(schedule, requestDate))
        .flatMap(this::createHourlyTimeRange)
        .distinct()
        .collect(Collectors.toList());

    Assertions.assertThat(availableTimes).isNotEmpty();
  }

  private Stream<LocalTime> createHourlyTimeRange(FacilityScheduleReadDto schedule) {
    LocalDateTime startDateTime = schedule.getStartDateTime();
    LocalDateTime endDateTime = schedule.getEndDateTime();

    LocalTime startTime = startDateTime.toLocalTime();
    LocalTime endTime = endDateTime.toLocalTime();

    int startHour = startTime.getHour();
    int endHour = endTime.getHour();

    return IntStream.rangeClosed(startHour, endHour)
        .mapToObj(hour -> LocalTime.of(hour, 0));
  }

  private boolean isMatchingDate(FacilityScheduleReadDto schedule, LocalDate requestDate) {
    LocalDateTime startDateTime = schedule.getStartDateTime();
    return startDateTime.toLocalDate().equals(requestDate);
  }


  @DisplayName("로그인한 멤버가 비지터일 경우 예약을 생성할 수 있다.")
  @Test
  public void createReservation_IfVisitor() {
    LocalDate requestDate = LocalDate.of(2023, 7, 2);

    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(requestDate.atTime(LocalTime.of(12, 0, 0)))
        .reservationEnd(requestDate.atTime(LocalTime.of(14, 59, 0)))
        .build();
    Long reservationId = reservationService.createReservation(dto, facilityRoomId,
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
    Long facilityId = 2L;
    LocalDate requestDate = LocalDate.of(2023, 7, 1);

    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(requestDate.atTime(LocalTime.of(12, 0, 0)))
        .reservationEnd(requestDate.atTime(LocalTime.of(14, 59, 0)))
        .build();
    Long reservationId = reservationService.createReservation(dto, facilityId,
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
    Long facilityId = 2L;
    LocalDate requestDate = LocalDate.of(2023, 7, 1);

    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(requestDate.atTime(LocalTime.of(12, 0, 0)))
        .reservationEnd(requestDate.atTime(LocalTime.of(14, 59, 0)))
        .build();
    Long reservationId = reservationService.createReservation(dto, facilityId,visitorMember.getEmail());
    reservationService.cancelReservation(reservationId, visitorMember.getEmail());

    ReservationReadDto readDto = reservationService.findReservation(reservationId, visitorMember.getEmail());

    assertAll(
        () -> assertThat(readDto.getId()).isEqualTo(reservationId),
        () -> assertThat(readDto.getMember().getEmail()).isEqualTo(visitorMember.getEmail()),
        () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.CANCELED)
    );
  }

  @DisplayName("예약을 생성한 사용자가 아닌 다른 사용자가 해당 예약을 취소하려고 한다면 예외가 발생한다")
  @Test
  public void cancelReservation_throwException_IfNotMyReservation() {
    Long facilityId = 2L;
    LocalDate requestDate = LocalDate.of(2023, 7, 3);

    ReservationCreateDto dto = ReservationCreateDto.builder()
        .reservationStart(requestDate.atTime(LocalTime.of(21, 0, 0)))
        .reservationEnd(requestDate.atTime(LocalTime.of(22, 59, 59)))
        .build();

    Long reservationId = reservationService.createReservation(dto, facilityId, visitorMember.getEmail());

    assertAll(
        () -> assertThatThrownBy(
            () -> reservationService.cancelReservation(reservationId, hostMember.getEmail()))
            .isInstanceOf(PermissionDeniedException.class)
    );
  }
}
