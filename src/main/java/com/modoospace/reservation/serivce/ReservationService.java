package com.modoospace.reservation.serivce;

import com.modoospace.exception.DuplicatedReservationException;
import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.reservation.controller.dto.AvailabilityTimeResponseDto;
import com.modoospace.reservation.controller.dto.ReservationCreateDto;
import com.modoospace.reservation.controller.dto.ReservationReadDto;
import com.modoospace.reservation.controller.dto.ReservationUpdateDto;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.space.controller.dto.facility.FacilityReadDto;
import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleReadDto;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.sevice.FacilityScheduleService;
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
  private final FacilityScheduleService facilityScheduleService;

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

  public AvailabilityTimeResponseDto getAvailabilityTime(Long facilityId, LocalDate searchDate) {

    //예약가능한 시간 가져오기
    List<LocalTime> availableTimes = getOpenTimes(facilityId, searchDate);

    //활성된 예약 가져오기
    Facility facility = findFacilityById(facilityId);
    List<Reservation> activeReservations = findActiveReservations(facility, searchDate);

    //이미 예약된 시간을 제외한 예약 가능한 시간 필터링
    for (Reservation reservation : activeReservations) {
      availableTimes = availableTimes.stream()
          .filter(time -> !reservation.isReservationBetween(time))
          .collect(Collectors.toList());
    }

    return AvailabilityTimeResponseDto.from(FacilityReadDto.toDto(facility), availableTimes);
  }

  private List<LocalTime> getOpenTimes(Long facilityId, LocalDate requestDate) {
    List<FacilityScheduleReadDto> facilitySchedules = facilityScheduleService
        .find1DayFacilitySchedules(facilityId, requestDate);

    return facilitySchedules.stream()
        .flatMap(this::createHourlyTimeRange)
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * 주어진 schedule 기반으로 시간대 범위를 생성하는 메소드입니다.
   */
  private Stream<LocalTime> createHourlyTimeRange(FacilityScheduleReadDto schedule) {
    int startHour = schedule.getStartDateTime().getHour();
    int endHour = schedule.getEndDateTime().getHour();

    return IntStream.rangeClosed(startHour, endHour)
        .mapToObj(hour -> LocalTime.of(hour, 0, 0));
  }

  private List<Reservation> findActiveReservations(Facility facility, LocalDate requestDate) {
    List<ReservationStatus> activeStatuses = ReservationStatus.getActiveStatuses();
    LocalDateTime startDateTime = LocalDateTime
        .of(requestDate, LocalTime.of(0, 0, 0));
    LocalDateTime endDateTime = LocalDateTime
        .of(requestDate, LocalTime.of(23, 59, 59));

    return reservationRepository
        .findByFacilityAndStatusInAndReservationStartBetween(facility, activeStatuses,
            startDateTime, endDateTime);
  }

  public ReservationReadDto findReservation(Long reservationId, String loginEmail) {
    Reservation reservation = findReservationById(reservationId);
    Member loginMember = findMemberByEmail(loginEmail);
    reservation.verifyReservationAccess(loginMember);
    return ReservationReadDto.toDto(reservation);
  }

  @Transactional
  public void updateStatus(Long reservationId, String loginEmail) {
    Reservation reservation = findReservationById(reservationId);
    Member loginMember = findMemberByEmail(loginEmail);
    reservation.approveReservation(loginMember);
  }

  @Transactional
  public void updateReservation(Long reservationId, ReservationUpdateDto reservationUpdateDto,
      String loginEmail) {
    Reservation reservation = findReservationById(reservationId);
    Member loginMember = findMemberByEmail(loginEmail);
    Reservation updatedReservation = reservationUpdateDto.toEntity(reservation);
    reservation.updateAsHost(updatedReservation, loginMember);
  }

  public List<ReservationReadDto> findAllAsVisitorByAdmin(Long memberId, String loginEmail) {
    Member admin = findMemberByEmail(loginEmail);
    admin.verifyRolePermission(Role.ADMIN);

    Member visitor = findMemberById(memberId);
    return findAllAsVisitor(visitor);
  }

  public List<ReservationReadDto> findAllAsVisitor(String loginEmail) {
    Member visitor = findMemberByEmail(loginEmail);
    visitor.verifyRolePermission(Role.VISITOR);

    return findAllAsVisitor(visitor);
  }

  private List<ReservationReadDto> findAllAsVisitor(Member visitor) {
    List<Reservation> reservations = reservationRepository.findByVisitor(visitor);

    return reservations.stream()
        .map(ReservationReadDto::toDto)
        .collect(Collectors.toList());
  }

  public List<ReservationReadDto> findAllAsHostByAdmin(Long memberId, String loginEmail) {
    Member admin = findMemberByEmail(loginEmail);
    admin.verifyRolePermission(Role.ADMIN);

    Member host = findMemberById(memberId);
    return findAllAsHost(host);
  }

  public List<ReservationReadDto> findAllAsHost(String loginEmail) {
    Member host = findMemberByEmail(loginEmail);
    host.verifyRolePermission(Role.HOST);

    return findAllAsHost(host);
  }

  private List<ReservationReadDto> findAllAsHost(Member host) {
    List<Reservation> reservations = reservationRepository.findByFacilitySpaceHost(host);

    return reservations.stream()
        .map(ReservationReadDto::toDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public void cancelReservation(Long reservationId, String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    Reservation reservation = findReservationById(reservationId);
    reservation.cancelAsVisitor(loginMember);
  }

  private Member findMemberById(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new NotFoundEntityException("사용자", memberId));
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
