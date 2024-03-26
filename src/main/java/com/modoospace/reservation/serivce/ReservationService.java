package com.modoospace.reservation.serivce;

import com.modoospace.alarm.controller.dto.AlarmEvent;
import com.modoospace.alarm.producer.AlarmProducer;
import com.modoospace.common.exception.ConflictingReservationException;
import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.common.exception.NotOpenedFacilityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import com.modoospace.member.service.MemberService;
import com.modoospace.reservation.controller.dto.*;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.space.controller.dto.facility.FacilityResponse;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.Schedule;
import com.modoospace.space.domain.ScheduleRepository;
import com.modoospace.space.repository.ScheduleQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
                                  Member loginMember) {
        Facility facility = findFacilityById(facilityId);
        Reservation reservation = createRequest.toEntity(facility, loginMember);
        verifyAvailability(facility, reservation);

        reservationRepository.save(reservation);

        AlarmEvent alarmEvent = AlarmEvent.ofNewReservationAlarm(reservation);
        alarmProducer.send(alarmEvent);

        return reservation.getId();
    }

    private void verifyAvailability(Facility facility, Reservation reservation) {
        facility.verifyReservationEnable();
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

    public ReservationResponse findReservation(Long reservationId, Member loginMember) {
        Reservation reservation = findReservationById(reservationId);
        reservation.verifyReservationAccess(loginMember);

        return ReservationResponse.of(reservation);
    }

    @Transactional
    public void approveReservation(Long reservationId, Member loginMember) {
        Reservation reservation = findReservationById(reservationId);
        reservation.approveAsHost(loginMember);

        AlarmEvent alarmEvent = AlarmEvent.ofApprovedReservationAlarm(reservation);
        alarmProducer.send(alarmEvent);
    }

    @Transactional
    public void updateReservation(Long reservationId, ReservationUpdateRequest updateRequest,
                                  Member loginMember) {
        Reservation reservation = findReservationById(reservationId);
        Reservation updatedReservation = updateRequest.toEntity(reservation);

        reservation.updateAsHost(updatedReservation, loginMember);
    }

    public List<ReservationResponse> findAllAsVisitorByAdmin(Long memberId, Member loginMember) {
        loginMember.verifyRolePermission(Role.ADMIN);

        Member visitor = memberService.findMemberById(memberId);
        return findAllAsVisitor(visitor);
    }

    public List<ReservationResponse> findAllAsVisitor(Member loginMember) {
        loginMember.verifyRolePermission(Role.VISITOR);

        List<Reservation> reservations = reservationRepository.findByVisitor(loginMember);
        return reservations.stream()
                .map(ReservationResponse::of)
                .collect(Collectors.toList());
    }

    public List<ReservationResponse> findAllAsHostByAdmin(Long memberId, Member loginMember) {
        loginMember.verifyRolePermission(Role.ADMIN);

        Member host = memberService.findMemberById(memberId);
        return findAllAsHost(host);
    }

    public List<ReservationResponse> findAllAsHost(Member loginMember) {
        loginMember.verifyRolePermission(Role.HOST);

        List<Reservation> reservations = reservationRepository.findByFacilitySpaceHost(loginMember);
        return reservations.stream()
                .map(ReservationResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelReservation(Long reservationId, Member loginMember) {
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
