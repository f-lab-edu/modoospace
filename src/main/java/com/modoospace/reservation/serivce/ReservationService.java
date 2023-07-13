package com.modoospace.reservation.serivce;

import com.modoospace.exception.DuplicatedReservationException;
import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.reservation.controller.dto.ReservationCreateDto;
import com.modoospace.reservation.controller.dto.ReservationReadDto;
import com.modoospace.reservation.controller.dto.ReservationUpdateDto;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
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

  private void validateAvailability(ReservationCreateDto createDto, Facility facility) {
    facility.validateFacilityAvailability(createDto);
    checkDuplicatedReservationTime(createDto, facility);
  }

  private void checkDuplicatedReservationTime(ReservationCreateDto createDto, Facility facility) {
    LocalDateTime start = createDto.getReservationStart();
    LocalDateTime end = createDto.getReservationEnd();

    Boolean isOverlappingReservation = reservationQueryRepository.isOverlappingReservation(facility.getId(), start, end);

    if (isOverlappingReservation) {
      throw new DuplicatedReservationException("동일한 시간대에 예약이 존재합니다.");
    }
  }

  public ReservationReadDto findReservation(Long reservationId, String loginEmail) {
    Reservation reservation = findReservationById(reservationId);
    Member loginMember = findMemberByEmail(loginEmail);
    verifyReservationAccess(loginMember,reservation);
    return ReservationReadDto.toDto(reservation);
  }

  @Transactional
  public void updateStatus(Long reservationId) {
    Reservation reservation = findReservationById(reservationId);
    Member host = reservation.getFacility().getSpace().getHost();
    reservation.approveReservation(host);
  }

  @Transactional
  public void updateReservation(Long reservationId, ReservationUpdateDto reservationUpdateDto,
      String loginEmail) {
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

  public List<ReservationReadDto> findAllAsHost(String loginEmail){
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
