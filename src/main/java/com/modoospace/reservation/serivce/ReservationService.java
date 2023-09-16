package com.modoospace.reservation.serivce;

import com.modoospace.alarm.controller.dto.AlarmEvent;
import com.modoospace.alarm.producer.AlarmProducer;
import com.modoospace.common.exception.ConflictingReservationException;
import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.common.exception.NotOpenedFacilityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import com.modoospace.member.service.MemberService;
import com.modoospace.reservation.controller.dto.AvailabilityNowResponseDto;
import com.modoospace.reservation.controller.dto.AvailabilityTimeResponseDto;
import com.modoospace.reservation.controller.dto.ReservationCreateDto;
import com.modoospace.reservation.controller.dto.ReservationReadDto;
import com.modoospace.reservation.controller.dto.ReservationUpdateDto;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.FacilitySchedule;
import com.modoospace.space.domain.FacilityScheduleRepository;
import com.modoospace.space.repository.FacilityScheduleQueryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReservationService {

  private final MemberService memberService;
  private final FacilityRepository facilityRepository;
  private final FacilityScheduleRepository facilityScheduleRepository;
  private final FacilityScheduleQueryRepository facilityScheduleQueryRepository;
  private final ReservationRepository reservationRepository;
  private final ReservationQueryRepository reservationQueryRepository;
  private final AlarmProducer alarmProducer;

  @Transactional
  public Long createReservation(ReservationCreateDto createDto, Long facilityId,
      String loginEmail) {
    Member visitor = memberService.findMemberByEmail(loginEmail);
    Facility facility = findFacilityById(facilityId);
    verifyPermissionAndAvailability(visitor, facility, createDto);

    Reservation reservation = createDto.toEntity(facility, visitor);
    reservationRepository.save(reservation);

    AlarmEvent alarmEvent = AlarmEvent.toNewReservationAlarm(reservation);
    alarmProducer.send(alarmEvent);

    return reservation.getId();
  }

  private void verifyPermissionAndAvailability(Member visitor, Facility facility,
      ReservationCreateDto createDto) {
    visitor.verifyRolePermission(Role.VISITOR);
    verifyFacilityAvailability(facility, createDto);
    verifyReservationAvailability(facility, createDto);
  }

  private void verifyFacilityAvailability(Facility facility, ReservationCreateDto createDto) {
    facility.verifyReservationEnable();
    verifyFacilitySchedulesOpen(facility, createDto);
  }

  private void verifyFacilitySchedulesOpen(Facility facility, ReservationCreateDto createDto) {
    Boolean isSchedulesOpen = facilityScheduleQueryRepository.isIncludingSchedule(
        facility, createDto.getReservationStart(), createDto.getReservationEnd());

    if (!isSchedulesOpen) {
      throw new NotOpenedFacilityException();
    }
  }

  private void verifyReservationAvailability(Facility facility, ReservationCreateDto createDto) {
    Boolean isOverlappingReservation = reservationQueryRepository.isOverlappingReservation(
        facility, createDto.getReservationStart(), createDto.getReservationEnd());

    if (isOverlappingReservation) {
      throw new ConflictingReservationException();
    }
  }

  public AvailabilityNowResponseDto getAvailabilityNow(Long facilityId) {
    Facility facility = findFacilityById(facilityId);
    if (!facility.getReservationEnable()) {
      return AvailabilityNowResponseDto.from(facility, false);
    }

    return AvailabilityNowResponseDto.from(facility, isAvailabilityNow(facility));
  }

  private Boolean isAvailabilityNow(Facility facility) {
    LocalDateTime now = LocalDateTime.now();
    Boolean isIncludingSchedule = facilityScheduleQueryRepository
        .isIncludingSchedule(facility, now, now);
    Boolean isOverlappingReservation = reservationQueryRepository
        .isOverlappingReservation(facility, now, now);

    return isIncludingSchedule && !isOverlappingReservation;
  }

  public AvailabilityTimeResponseDto getAvailabilityTime(Long facilityId, LocalDate searchDate) {
    Facility facility = findFacilityById(facilityId);
    List<FacilitySchedule> facilitySchedules = findOpenFacilitySchedules(facility, searchDate);
    List<Reservation> reservations = findActiveReservations(facility, searchDate);

    //이미 예약된 시간을 제외한 예약 가능한 시간 필터링
    List<LocalTime> availableTimes = createHourlyTimeRange(facilitySchedules, reservations);

    return AvailabilityTimeResponseDto.from(facility, availableTimes);
  }

  private List<FacilitySchedule> findOpenFacilitySchedules(Facility facility,
      LocalDate requestDate) {
    LocalDateTime startDateTime = requestDate.atTime(0, 0, 0);
    LocalDateTime endDateTime = requestDate.atTime(23, 59, 59);

    return facilityScheduleRepository.findByFacilityAndStartDateTimeBetween(
        facility, startDateTime, endDateTime);
  }

  private List<Reservation> findActiveReservations(Facility facility, LocalDate requestDate) {
    List<ReservationStatus> activeStatuses = ReservationStatus.getActiveStatuses();
    LocalDateTime startDateTime = requestDate.atTime(0, 0, 0);
    LocalDateTime endDateTime = requestDate.atTime(23, 59, 59);

    return reservationRepository.findByFacilityAndStatusInAndReservationStartBetween(
        facility, activeStatuses, startDateTime, endDateTime);
  }

  private List<LocalTime> createHourlyTimeRange(List<FacilitySchedule> facilitySchedules,
      List<Reservation> reservations) {
    List<LocalTime> localTimes = facilitySchedules.stream()
        .flatMap(facilitySchedule -> facilitySchedule.createHourlyTimeRange().stream())
        .collect(Collectors.toList());

    return calculateHourlyTimeRange(localTimes, reservations);
  }

  private List<LocalTime> calculateHourlyTimeRange(List<LocalTime> localTimes,
      List<Reservation> reservations) {
    for (Reservation reservation : reservations) {
      localTimes = localTimes.stream()
          .filter(time -> !reservation.isReservationBetween(time))
          .collect(Collectors.toList());
    }
    return localTimes;
  }

  public ReservationReadDto findReservation(Long reservationId, String loginEmail) {
    Reservation reservation = findReservationById(reservationId);
    Member loginMember = memberService.findMemberByEmail(loginEmail);
    reservation.verifyReservationAccess(loginMember);
    return ReservationReadDto.toDto(reservation);
  }

  @Transactional
  public void updateStatus(Long reservationId, String loginEmail) {
    Reservation reservation = findReservationById(reservationId);
    Member loginMember = memberService.findMemberByEmail(loginEmail);
    reservation.approveReservation(loginMember);

    AlarmEvent alarmEvent = AlarmEvent.toApprovedReservationAlarm(reservation);
    alarmProducer.send(alarmEvent);
  }

  @Transactional
  public void updateReservation(Long reservationId, ReservationUpdateDto reservationUpdateDto,
      String loginEmail) {
    Reservation reservation = findReservationById(reservationId);
    Member loginMember = memberService.findMemberByEmail(loginEmail);
    Reservation updatedReservation = reservationUpdateDto.toEntity(reservation);
    reservation.updateAsHost(updatedReservation, loginMember);
  }

  public List<ReservationReadDto> findAllAsVisitorByAdmin(Long memberId, String loginEmail) {
    Member admin = memberService.findMemberByEmail(loginEmail);
    admin.verifyRolePermission(Role.ADMIN);

    Member visitor = memberService.findMemberById(memberId);
    return findAllAsVisitor(visitor);
  }

  public List<ReservationReadDto> findAllAsVisitor(String loginEmail) {
    Member visitor = memberService.findMemberByEmail(loginEmail);
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
    Member admin = memberService.findMemberByEmail(loginEmail);
    admin.verifyRolePermission(Role.ADMIN);

    Member host = memberService.findMemberById(memberId);
    return findAllAsHost(host);
  }

  public List<ReservationReadDto> findAllAsHost(String loginEmail) {
    Member host = memberService.findMemberByEmail(loginEmail);
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
    Member loginMember = memberService.findMemberByEmail(loginEmail);
    Reservation reservation = findReservationById(reservationId);
    reservation.cancelAsVisitor(loginMember);

    AlarmEvent alarmEvent = AlarmEvent.toCancelReservationAlarm(reservation);
    alarmProducer.send(alarmEvent);
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
