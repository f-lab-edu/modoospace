package com.modoospace.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.reservation.controller.dto.AvailabilityTimeRequestDto;
import com.modoospace.reservation.controller.dto.AvailabilityTimeResponseDto;
import com.modoospace.reservation.controller.dto.ReservationCreateDto;
import com.modoospace.reservation.controller.dto.ReservationReadDto;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.reservation.serivce.ReservationService;
import com.modoospace.space.controller.dto.facility.FacilityReadDetailDto;
import com.modoospace.space.controller.dto.facility.FacilityReadDto;
import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleReadDto;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.sevice.FacilityService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@SpringBootTest
@SpringJUnitConfig
public class ReservationServiceTest {

  private ReservationService reservationService;

  @Autowired
  private ReservationRepository reservationRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityService facilityService;

  @Autowired
  private ReservationQueryRepository reservationQueryRepository;

  private ReservationCreateDto reservationCreateDto;

  private Member member;

  @BeforeEach
  public void setUp() {
    reservationService = new ReservationService(reservationRepository, facilityRepository,memberRepository, reservationQueryRepository,facilityService);
    LocalDateTime requestStart = LocalDateTime.of(2023, 7, 1, 10, 0);
    reservationCreateDto = createReservationDto(requestStart, requestStart.plusHours(3));
    member = getMember();
  }

  @DisplayName("방문자는 선택한 시설과 날짜에 대한 예약 가능 여부를 확인할 수 있다.")
  @Test
  public void checkAvailableDate() {
    FacilityReadDetailDto facility = facilityService.findFacility(2L);
    List<FacilityScheduleReadDto> schedules = facility.getFacilitySchedules();

    LocalDate requestDate = LocalDate.of(2023, 7, 3);

    boolean hasMatchingDate = schedules.stream()
        .anyMatch(schedule -> schedule.getStartDateTime().toLocalDate().equals(requestDate));

    assertThat(hasMatchingDate).isTrue();
  }

  @DisplayName("visitor는 시설의 예약 가능한 시간을 시간 단위(HH:mm:ss)로 조회할 수 있다.")
  @Test
  void testGetAvailability() {
    Long facilityId = 2L;
    LocalDate requestDate = LocalDate.of(2023, 7, 2);
    FacilityReadDetailDto facility = facilityService.findFacility(facilityId);
    FacilityReadDto facilityReadDto = FacilityReadDto.toDto(facility(facilityId));

    //시설의 미래 3개월치 스케줄 조회
    List<FacilityScheduleReadDto> facilitySchedules = facility.getFacilitySchedules();

    // 요청된 날짜에 대한 가능한 시간 가져오기
    List<LocalTime> availableTimes = reservationService.getAvailableTimes(requestDate, facilitySchedules);

    // 요청된 날짜에 대한 예약된 시간 가져오기
    List<LocalTime> reservedTimes = reservationService.getReservedTimes(requestDate,facilityId);

    // 예약된 시간을 제외한 가능한 시간 필터링
    List<LocalTime> availableTimesWithoutReservation = availableTimes.stream()
        .filter(time -> !reservedTimes.contains(time))
        .collect(Collectors.toList());
    AvailabilityTimeResponseDto expectedResponse = new AvailabilityTimeResponseDto(facilityReadDto, availableTimesWithoutReservation);

    // 결과
    AvailabilityTimeRequestDto requestDto = new AvailabilityTimeRequestDto(facilityId, requestDate);
    AvailabilityTimeResponseDto response = reservationService.getAvailabilityTime(facilityId,requestDto);

    // 결과 검증
    assertThat(response.getFacility().getId()).isEqualTo(expectedResponse.getFacility().getId());
    assertThat(expectedResponse.getAvailableTimes()).isEqualTo(response.getAvailableTimes());

    // 결과 출력
    log.info("==== 예약 가능한 시설 및 날짜 ====");
    log.info("시설: {}", facility.getName());
    log.info("날짜: {}", requestDate);
    log.info("==== 예약 가능한 시간 (예약된 시간 제외) ====");
    availableTimesWithoutReservation.forEach(time -> log.info("{}", time));
  }

  @DisplayName("특정 예약일에 예약상태가 완료, 대기중인 예약을 조회할 수 있다.")
  @Test
  public void findActiveReservations() {
    // Given
    Long facilityId = 2L;
    LocalDate requestDate = LocalDate.of(2023, 7, 2);
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
    activeReservations.forEach(id -> log.info("{}", id));
  }

  @DisplayName("특정 날짜의 예약가능시간을 조회할 수 있다.")
  @Test
  public void getAvailableTimes() {
    // 시설 선택
    Long facilityId = 2L;
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
    availableTimes.forEach(time -> log.info("{}", time));
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
    FacilityReadDetailDto facilityReadDetailDto = facilityService.findFacility(1L);

    Reservation reservation = reservationService.createReservation(reservationCreateDto,
        facilityReadDetailDto.getId(), member.getEmail());

    ReservationReadDto readDto = reservationService.findReservation(reservation.getId(),
        member.getEmail());

    assertAll(
        () -> assertThat(readDto.getId()).isEqualTo(reservation.getId()),
        () -> assertThat(readDto.getFacility().getId()).isEqualTo(
            reservation.getFacility().getId()),
        () -> assertThat(readDto.getMember().getId()).isEqualTo(member.getId()),
        () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.WAITING)
    );
  }

  @DisplayName("좌석을 예약할 경우 예약상태는 완료가 된다.")
  @Test
  public void reservationStatus_IfSeat() {
    List<Facility> facilities = facilityRepository.findAll();
    Reservation reservation = reservationService.createReservation(reservationCreateDto,
        facilities.get(1).getId(),
        member.getEmail());

    ReservationReadDto readDto = reservationService.findReservation(reservation.getId(),
        member.getEmail());
    assertAll(
        () -> assertThat(readDto.getId()).isEqualTo(reservation.getId()),
        () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.COMPLETED)
    );
  }

  @Transactional
  @DisplayName("방문자가 본인의 예약을 취소할 수 있다.")
  @Test
  public void cancelReservation() {
    Optional<Reservation> reservationOptional = reservationRepository.findById(3L);
    Reservation reservation = reservationOptional.orElseThrow(
        () -> new NotFoundEntityException("예약"));

    reservation.updateStatusToCanceled(member);

    ReservationReadDto readDto = reservationService.findReservation(reservation.getId(),
        member.getEmail());

    assertAll(
        () -> assertThat(readDto.getId()).isEqualTo(reservation.getId()),
        () -> assertThat(readDto.getId()).isEqualTo(member.getId()),
        () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.CANCELED)
    );
  }

  @DisplayName("예약을 생성한 사용자가 아닌 다른 사용자가 해당 예약을 취소하려고 한다면 예외가 발생한다")
  @Test
  public void cancelReservation_throwException_IfNotMyReservation() {
    String otherUserEmail = "gjwjdghk123@gmail.com";

    Optional<Reservation> reservationOptional = reservationRepository.findById(2L);
    Reservation reservation = reservationOptional.orElseThrow(
        () -> new NotFoundEntityException("예약"));

    assertAll(
        () -> assertThatThrownBy(
            () -> reservationService.cancelReservation(reservation.getId(), otherUserEmail))
            .isInstanceOf(PermissionDeniedException.class)
    );
  }

  private ReservationCreateDto createReservationDto(LocalDateTime reservationStart,
      LocalDateTime reservationEnd) {
    return ReservationCreateDto.builder()
        .reservationStart(reservationStart)
        .reservationEnd(reservationEnd)
        .build();
  }

  private Member getMember() {
    Optional<Member> memberOptional = memberRepository.findByEmail("yh.kim@jr.naver.com");
    return memberOptional.orElseThrow(
        () -> new NotFoundEntityException("사용자", "yh.kim@jr.naver.com"));
  }

  private Facility facility(Long facilityId) {
    Optional<Facility> facilityOptional = facilityRepository.findById(facilityId);
    return facilityOptional.orElseThrow(
        () -> new NotFoundEntityException("시설"));
  }
}
