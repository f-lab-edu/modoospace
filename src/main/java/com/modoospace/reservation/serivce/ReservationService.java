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
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.space.controller.dto.facility.FacilityReadDetailDto;
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
  public Reservation createReservation(ReservationCreateDto createDto, Long facilityId,
      String loginEmail) {
    Facility facility = findFacilityById(facilityId);
    validateAvailability(createDto, facility);
    Member visitor = findMemberByEmail(loginEmail);
    Reservation reservation = createDto.toEntity(facility, visitor);
    reservationRepository.save(reservation);

    return reservation;
  }

  public AvailabilityTimeResponseDto getAvailabilityTime(Long facilityId, AvailabilityTimeRequestDto requestDto) {
    LocalDate requestDate = requestDto.getRequestDate();
    FacilityReadDetailDto facility = facilityService.findFacility(facilityId);

    //시설의 스케줄 조회
    List<FacilityScheduleReadDto> facilitySchedules = facility.getFacilitySchedules();

    // 요청된 날짜에 대한 가능한 시간 가져오기
    List<LocalTime> availableTimes = getAvailableTimes(requestDate, facilitySchedules);

    // 요청된 날짜에 대한 예약된 시간 가져오기
    List<LocalTime> reservedTimes = getReservedTimes(requestDate);

    // 예약된 시간을 제외한 가능한 시간 필터링
    List<LocalTime> availableTimesWithoutReservation = availableTimes.stream()
        .filter(time -> !reservedTimes.contains(time))
        .collect(Collectors.toList());

    // 가능한 시간 응답 DTO 생성 및 반환
    return AvailabilityTimeResponseDto.from(facilityId, availableTimesWithoutReservation);
  }

  public List<LocalTime> getReservedTimes(LocalDate requestDate) {
    return reservationRepository.findAll().stream()
        .filter(reservation -> reservation.getReservationStart().toLocalDate().equals(requestDate))
        .flatMap(reservation -> {
          LocalTime startTime = reservation.getReservationStart().toLocalTime();
          LocalTime endTime = reservation.getReservationEnd().toLocalTime();
          return Stream.iterate(startTime, time -> time.plusHours(1))
              .limit(Duration.between(startTime, endTime).toHours() + 1);
        })
        .distinct()
        .collect(Collectors.toList());
  }

  public List<LocalTime> getAvailableTimes(LocalDate requestDate,
      List<FacilityScheduleReadDto> facilitySchedules) {
    return facilitySchedules.stream()
        .filter(schedule -> schedule.getStartDateTime().toLocalDate().equals(requestDate))
        .flatMap(schedule -> {
          LocalTime startTime = schedule.getStartDateTime().toLocalTime();
          LocalTime endTime = schedule.getEndDateTime().toLocalTime();
          return IntStream.range(startTime.getHour(), endTime.getHour() + 1)
              .mapToObj(hour -> LocalTime.of(hour, 0));
        })
        .distinct()
        .collect(Collectors.toList());
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
