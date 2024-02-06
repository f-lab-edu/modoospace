package com.modoospace.reservation.serivce;

import com.modoospace.alarm.controller.dto.AlarmEvent;
import com.modoospace.alarm.producer.AlarmProducer;
import com.modoospace.common.exception.ConflictingReservationException;
import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.common.exception.NotOpenedFacilityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import com.modoospace.member.service.MemberService;
import com.modoospace.reservation.controller.dto.AvailabilityTimeResponse;
import com.modoospace.reservation.controller.dto.ReservationCreateRequest;
import com.modoospace.reservation.controller.dto.ReservationResponse;
import com.modoospace.reservation.controller.dto.ReservationUpdateRequest;
import com.modoospace.reservation.controller.dto.TimeResponse;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.space.controller.dto.facility.FacilityResponse;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.Schedule;
import com.modoospace.space.domain.ScheduleRepository;
import com.modoospace.space.repository.ScheduleQueryRepository;
import java.time.LocalDate;
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
    private final ScheduleRepository scheduleRepository;
    private final ScheduleQueryRepository scheduleQueryRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationQueryRepository reservationQueryRepository;
    private final AlarmProducer alarmProducer;

    @Transactional
    public Long createReservation(ReservationCreateRequest createRequest, Long facilityId,
        String loginEmail) {
        Member visitor = memberService.findMemberByEmail(loginEmail);
        Facility facility = findFacilityById(facilityId);
        Reservation reservation = createRequest.toEntity(facility, visitor);
        verifyAvailability(facility, reservation);

        reservationRepository.save(reservation);

        AlarmEvent alarmEvent = AlarmEvent.ofNewReservationAlarm(reservation);
        alarmProducer.send(alarmEvent);

        return reservation.getId();
    }

    private void verifyAvailability(Facility facility, Reservation reservation) {
        verifyFacilitySchedulesOpen(facility, reservation);
        verifyReservationAvailability(facility, reservation);
    }

    private void verifyFacilitySchedulesOpen(Facility facility, Reservation reservation) {
        Boolean isSchedulesOpen = scheduleQueryRepository.isIncludingSchedule(
            facility, reservation.getDateTimeRange());

        if (!isSchedulesOpen) {
            throw new NotOpenedFacilityException();
        }
    }

    private void verifyReservationAvailability(Facility facility, Reservation reservation) {
        Boolean conflictingReservation = reservationQueryRepository.isConflictingReservation(
            facility, reservation.getDateTimeRange());

        if (conflictingReservation) {
            throw new ConflictingReservationException();
        }
    }

    public AvailabilityTimeResponse getAvailabilityTime(Long facilityId, LocalDate searchDate) {
        Facility facility = findFacilityById(facilityId);
        List<Schedule> schedules = scheduleRepository.findByFacilityAndDate(facility, searchDate);
        List<Reservation> reservations = reservationQueryRepository.findActiveReservations(facility,
            searchDate);

        List<TimeResponse> timeResponse = TimeResponse.createTimeResponse(schedules, reservations,
            searchDate);
        return new AvailabilityTimeResponse(FacilityResponse.of(facility), timeResponse);
    }

    public ReservationResponse findReservation(Long reservationId, String loginEmail) {
        Reservation reservation = findReservationById(reservationId);
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        reservation.verifyReservationAccess(loginMember);

        return ReservationResponse.of(reservation);
    }

    @Transactional
    public void approveReservation(Long reservationId, String loginEmail) {
        Reservation reservation = findReservationById(reservationId);
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        reservation.approveAsHost(loginMember);

        AlarmEvent alarmEvent = AlarmEvent.ofApprovedReservationAlarm(reservation);
        alarmProducer.send(alarmEvent);
    }

    @Transactional
    public void updateReservation(Long reservationId, ReservationUpdateRequest updateRequest,
        String loginEmail) {
        Reservation reservation = findReservationById(reservationId);
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Reservation updatedReservation = updateRequest.toEntity(reservation);

        reservation.updateAsHost(updatedReservation, loginMember);
    }

    public List<ReservationResponse> findAllAsVisitorByAdmin(Long memberId, String loginEmail) {
        Member admin = memberService.findMemberByEmail(loginEmail);
        admin.verifyRolePermission(Role.ADMIN);

        Member visitor = memberService.findMemberById(memberId);
        return findAllAsVisitor(visitor);
    }

    public List<ReservationResponse> findAllAsVisitor(String loginEmail) {
        Member visitor = memberService.findMemberByEmail(loginEmail);
        visitor.verifyRolePermission(Role.VISITOR);

        return findAllAsVisitor(visitor);
    }

    private List<ReservationResponse> findAllAsVisitor(Member visitor) {
        List<Reservation> reservations = reservationRepository.findByVisitor(visitor);

        return reservations.stream()
            .map(ReservationResponse::of)
            .collect(Collectors.toList());
    }

    public List<ReservationResponse> findAllAsHostByAdmin(Long memberId, String loginEmail) {
        Member admin = memberService.findMemberByEmail(loginEmail);
        admin.verifyRolePermission(Role.ADMIN);

        Member host = memberService.findMemberById(memberId);
        return findAllAsHost(host);
    }

    public List<ReservationResponse> findAllAsHost(String loginEmail) {
        Member host = memberService.findMemberByEmail(loginEmail);
        host.verifyRolePermission(Role.HOST);

        return findAllAsHost(host);
    }

    private List<ReservationResponse> findAllAsHost(Member host) {
        List<Reservation> reservations = reservationRepository.findByFacilitySpaceHost(host);

        return reservations.stream()
            .map(ReservationResponse::of)
            .collect(Collectors.toList());
    }

    @Transactional
    public void cancelReservation(Long reservationId, String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Reservation reservation = findReservationById(reservationId);
        reservation.cancelAsVisitor(loginMember);

        AlarmEvent alarmEvent = AlarmEvent.ofCancelReservationAlarm(reservation);
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
