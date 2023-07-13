package com.modoospace.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.reservation.controller.dto.ReservationCreateDto;
import com.modoospace.reservation.controller.dto.ReservationReadDto;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.reservation.serivce.ReservationService;
import com.modoospace.space.controller.dto.facility.FacilityReadDetailDto;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.sevice.FacilityService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
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
    reservationService = new ReservationService(reservationRepository, facilityRepository,memberRepository, reservationQueryRepository);
    LocalDateTime requestStart = LocalDateTime.of(2023, 7, 1, 10, 0);
    reservationCreateDto = createReservationDto(requestStart, requestStart.plusHours(3));
    member = getMember();
  }

  @DisplayName("visitor는 선택한 시설의 예약가능한 날짜를 조회할 수 있다.")
  @Test
  public void checkAvailable(){
    Optional<Facility> optionalFacility = facilityRepository.findById(1L);
    Facility facility = optionalFacility.orElseThrow(() -> new NotFoundEntityException("시설"));


  }

  @DisplayName("로그인한 멤버가 비지터일 경우 예약을 생성할 수 있다.")
  @Test
  public void createReservation_IfVisitor() {
    FacilityReadDetailDto facilityReadDetailDto = facilityService.findFacility(1L);

    Reservation reservation = reservationService.createReservation(reservationCreateDto, facilityReadDetailDto.getId(), member.getEmail());

    ReservationReadDto readDto = reservationService.findReservation(reservation.getId(), member.getEmail());

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
  public void reservationStatus_IfSeat() {    List<Facility> facilities = facilityRepository.findAll();
    Reservation reservation = reservationService.createReservation(reservationCreateDto,
        facilities.get(1).getId(),
        member.getEmail());

    ReservationReadDto readDto = reservationService.findReservation(reservation.getId(), member.getEmail());
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

    ReservationReadDto readDto = reservationService.findReservation(reservation.getId(), member.getEmail());

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
}
