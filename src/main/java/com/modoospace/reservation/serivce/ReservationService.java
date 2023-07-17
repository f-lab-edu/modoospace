package com.modoospace.reservation.serivce;

import com.modoospace.exception.DuplicatedReservationException;
import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.reservation.controller.dto.AvailabilityTimeRequestDto;
import com.modoospace.reservation.controller.dto.AvailabilityTimeResponseDto;
import com.modoospace.reservation.controller.dto.ReservationCreateDto;
import com.modoospace.reservation.controller.dto.ReservationReadDto;
import com.modoospace.reservation.controller.dto.ReservationUpdateDto;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.space.controller.dto.facility.FacilityReadDetailDto;
import com.modoospace.space.controller.dto.facility.FacilityReadDto;
import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleReadDto;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.sevice.FacilityService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReservationService {

  private final ReservationRepository reservationRepository;
  private final FacilityRepository facilityRepository;
  private final MemberRepository memberRepository;
  private final ReservationQueryRepository reservationQueryRepository;
  private final FacilityService facilityService;

  @Transactional
  public Long createReservation(ReservationCreateDto createDto, Long facilityId,
      String loginEmail) {
    Facility facility = findFacilityById(facilityId);
    validateAvailability(createDto, facility);
    Member visitor = findMemberByEmail(loginEmail);
    Reservation reservation = createDto.toEntity(facility, visitor);
    reservationRepository.save(reservation);

    return reservation.getId();
  }

  public AvailabilityTimeResponseDto getAvailabilityTime(Long facilityId, AvailabilityTimeRequestDto requestDto) {
    LocalDate requestDate = requestDto.getRequestDate();

    //시설의 스케줄 조회
    FacilityReadDetailDto facility = facilityService.findFacility(facilityId);
    List<FacilityScheduleReadDto> facilitySchedules = facility.getFacilitySchedules();

    //예약가능한 시간 가져오기
    List<LocalTime> availableTimes = getAvailableTimes(requestDate, facilitySchedules);

    //예악된 시간 가져오기
    List<LocalTime> reservedTimes = getReservedTimes(requestDate, facilityId);

    //이미 예약된 시간을 제외한 예약 가능한 시간 필터링
    List<LocalTime> availableTimesWithoutReservation = availableTimes.stream()
        .filter(time -> !reservedTimes.contains(time))
        .collect(Collectors.toList());

    FacilityReadDto facilityReadDto = FacilityReadDto.toDto(findFacilityById(facilityId));

    return AvailabilityTimeResponseDto.from(facilityReadDto, availableTimesWithoutReservation);
  }

  public List<LocalTime> getReservedTimes(LocalDate requestDate, Long facilityId) {
    //시설의 요청 날짜로 기존 예약리스트 조회
    List<Reservation> activeReservations = findActiveReservations(requestDate, facilityId);

    return activeReservations.stream()
        .flatMap(reservation -> getReservationTimes(reservation).stream())
        .collect(Collectors.toList());
  }

  private List<Reservation> findActiveReservations(LocalDate requestDate, Long facilityId) {
    LocalDateTime startDateTime = requestDate.atStartOfDay();
    LocalDateTime endDateTime = requestDate.atTime(LocalTime.MAX);
    List<ReservationStatus> activeStatuses = ReservationStatus.getActiveStatuses();
    return reservationRepository.findByReservationStartBetweenAndStatusInAndFacility_Id(
        startDateTime,endDateTime,activeStatuses, facilityId);
  }

  private List<LocalTime> getReservationTimes(Reservation reservation) {
    // 예약 객체에서 시작 시간과 종료 시간
    LocalTime startTime = reservation.getReservationStart().toLocalTime();
    LocalTime endTime = reservation.getReservationEnd().toLocalTime();

    // 시작 시간부터 종료 시간까지 1시간 간격으로 반복
    return Stream.iterate(startTime, time -> time.plusHours(1))
        .limit(Duration.between(startTime, endTime).toHours() + 1)
        .collect(Collectors.toList());
  }

  public List<LocalTime> getAvailableTimes(LocalDate requestDate, List<FacilityScheduleReadDto> facilitySchedules) {
    return facilitySchedules.stream()
        .filter(schedule -> isMatchingDate(schedule, requestDate))
        .flatMap(this::createHourlyTimeRange)
        .distinct()
        .collect(Collectors.toList());
  }

  private boolean isMatchingDate(FacilityScheduleReadDto schedule, LocalDate requestDate) {
    LocalDateTime startDateTime = schedule.getStartDateTime();
    return startDateTime.toLocalDate().equals(requestDate);
  }

  /**
   * 주어진 schedule 기반으로 시간대 범위를 생성하는 메소드입니다.
   *
   * @param schedule 시간대 범위를 생성할 FacilityScheduleReadDto
   * @return 시간대 범위
   */
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

  private void validateAvailability(ReservationCreateDto createDto, Facility facility) {
    facility.validateFacilityAvailability(createDto);
    checkDuplicatedReservationTime(createDto, facility);
  }

  private void checkDuplicatedReservationTime(ReservationCreateDto createDto, Facility facility) {
    LocalDateTime start = createDto.getReservationStart();
    LocalDateTime end = createDto.getReservationEnd();

    Boolean isOverlappingReservation = reservationQueryRepository.isOverlappingReservation(
        facility.getId(), start, end);

    if (isOverlappingReservation) {
      throw new DuplicatedReservationException("동일한 시간대에 예약이 존재합니다.");
    }
  }

  public ReservationReadDto findReservation(Long reservationId, String loginEmail) {
    Reservation reservation = findReservationById(reservationId);
    Member loginMember = findMemberByEmail(loginEmail);
    verifyReservationAccess(loginMember, reservation);
    return ReservationReadDto.toDto(reservation);
  }

  @Transactional
  public void updateStatus(Long reservationId) {
    Reservation reservation = findReservationById(reservationId);
    Member host = reservation.getFacility().getSpace().getHost();
    reservation.approveReservation(host);
  }

  @Transactional
  public void updateReservation(Long reservationId, ReservationUpdateDto reservationUpdateDto, String loginEmail) {
    Reservation reservation = findReservationById(reservationId);
    Member loginMember = findMemberByEmail(loginEmail);
    Reservation updatedReservation = reservationUpdateDto.toEntity(reservation);
    reservation.updateAsHost(updatedReservation, loginMember);
  }

  public List<ReservationReadDto> findAll(String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    List<Reservation> reservations = reservationRepository.findByVisitor(loginMember);
    reservations.forEach(reservation -> verifyReservationAccess(loginMember, reservation));

    return reservations.stream()
        .map(ReservationReadDto::toDto)
        .collect(Collectors.toList());
  }

  public List<ReservationReadDto> findAllAsHost(String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    List<Reservation> reservations = reservationRepository.findByFacilitySpaceHost(loginMember);
    reservations.forEach(reservation -> verifyReservationAccess(loginMember, reservation));

    return reservations.stream()
        .map(ReservationReadDto::toDto)
        .collect(Collectors.toList());
  }

  private void verifyReservationAccess(Member loginMember, Reservation reservation) {
    Role memberRole = loginMember.getRole();

    if (Role.ADMIN.equals(memberRole) || Role.HOST.equals(memberRole)) {
      reservation.verifyHostRole(loginMember);
    } else {
      reservation.verifySameVisitor(loginMember);
    }
  }

  @Transactional
  public void cancelReservation(Long reservationId, String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    Reservation reservation = findReservationById(reservationId);
    reservation.updateStatusToCanceled(loginMember);
  }

  private Member findMemberByEmail(String email) {
    return memberRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEntityException("사용자", email));
  }

  private Reservation findReservationById(Long reservationId) {
    return reservationRepository.findById(reservationId)
        .orElseThrow(() -> new NotFoundEntityException("예약", reservationId));
  }

  private Facility findFacilityById(Long facilityId) {
    return facilityRepository.findById(facilityId)
        .orElseThrow(() -> new NotFoundEntityException("시설", facilityId));
  }

}
