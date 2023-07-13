package com.modoospace.reservation.serivce;

import com.modoospace.exception.DuplicatedReservationException;
import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.reservation.controller.dto.ReservationCreateDto;
import com.modoospace.reservation.controller.dto.ReservationReadDto;
import com.modoospace.reservation.controller.dto.ReservationUpdateDto;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.FacilitySchedule;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReservationService {

  private final ReservationRepository reservationRepository;
  private final FacilityRepository facilityRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public Reservation createReservation(ReservationCreateDto createDto, Long facilityId, String loginEmail) {
    Facility facility = findFacilityById(facilityId);
    validateAvailability(createDto, facility);
    Member visitor = findMemberByEmail(loginEmail);
    Reservation reservation = createDto.toEntity(facility, visitor);
    reservationRepository.save(reservation);

    return reservation;
  }

  private void validateAvailability(ReservationCreateDto createDto, Facility facility) {
    facility.validateFacilityAvailability(createDto);
    checkTimeOverlapWithFacilitySchedules(createDto, facility);
  }

  private void checkTimeOverlapWithFacilitySchedules(ReservationCreateDto createDto, Facility facility) {
    LocalDateTime reservationStart = createDto.getReservationStart();
    LocalDateTime reservationEnd = createDto.getReservationEnd();

    List<ReservationReadDto> existingReservations = reservationRepository.findByFacilityId(facility.getId());
    boolean isOverlap = existingReservations.stream()
        .anyMatch(existingReservation ->
            isTimeOverlap(reservationStart, reservationEnd, existingReservation.getReservationStart(), existingReservation.getReservationEnd()));

    if (isOverlap) {
      throw new DuplicatedReservationException("해당 시간에 중복된 예약이 있습니다.");
    }
  }

  private boolean isTimeOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
    return start1.isBefore(end2) && end1.isAfter(start2);
  }

  public ReservationReadDto findReservation(Long reservationId) {
    Reservation reservation = findReservationById(reservationId);
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
    return reservationRepository.findByVisitor(loginMember);
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
