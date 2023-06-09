package com.modoospace.reservation.serivce;

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
  public Reservation createReservation(ReservationCreateDto createDto, Long facilityId,
      String loginEmail) {
    Facility facility = findFacilityById(facilityId);
    //TODO : 시설이용이 가능한 상태인지 조회 필요
    Member visitor = findMemberByEmail(loginEmail);

    Reservation reservation = createDto.toEntity(facility, visitor);
    reservationRepository.save(reservation);

    return reservation;
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
    Facility facility = facilityRepository.findById(facilityId)
        .orElseThrow(() -> new NotFoundEntityException("시설", facilityId));
    return facility;
  }
}
