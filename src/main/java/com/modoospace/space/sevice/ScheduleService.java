package com.modoospace.space.sevice;

import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.service.MemberService;
import com.modoospace.space.controller.dto.schedule.ScheduleCreateUpdateRequest;
import com.modoospace.space.controller.dto.schedule.ScheduleResponse;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.Schedule;
import com.modoospace.space.domain.ScheduleRepository;
import com.modoospace.space.repository.ScheduleQueryRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ScheduleService {

    private final MemberService memberService;
    private final FacilityRepository facilityRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleQueryRepository scheduleQueryRepository;

    @Transactional
    public void createSchedule(Long facilityId, ScheduleCreateUpdateRequest createRequest,
        String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Facility facility = findFacilityById(facilityId);
        facility.verifyManagementPermission(loginMember);

        facility.addSchedule(createRequest.toEntity(facility));
    }

    public ScheduleResponse findSchedule(Long facilityScheduleId) {
        Schedule schedule = findScheduleById(facilityScheduleId);

        return ScheduleResponse.of(schedule);
    }

    @Transactional
    public void updateSchedule(Long facilityScheduleId, ScheduleCreateUpdateRequest updateRequest,
        String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Schedule schedule = findScheduleById(facilityScheduleId);
        Facility facility = schedule.getFacility();
        facility.verifyManagementPermission(loginMember);

        facility.updateSchedule(updateRequest.toEntity(facility), schedule);
    }

    @Transactional
    public void deleteSchedule(Long facilityScheduleId, String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Schedule schedule = findScheduleById(facilityScheduleId);
        Facility facility = schedule.getFacility();
        facility.verifyManagementPermission(loginMember);

        scheduleRepository.delete(schedule);
    }

    public List<ScheduleResponse> find1DaySchedules(Long facilityId,
        LocalDate findDate) {
        Facility facility = findFacilityById(facilityId);
        List<Schedule> schedules = scheduleQueryRepository
            .find1DaySchedules(facility, findDate);

        return schedules.stream()
            .map(ScheduleResponse::of)
            .collect(Collectors.toList());
    }

    @Transactional
    public void create1MonthDefaultSchedules(Long facilityId, YearMonth createYearMonth,
        String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Facility facility = findFacilityById(facilityId);
        facility.verifyManagementPermission(loginMember);

        scheduleQueryRepository.delete1MonthSchedules(facility, createYearMonth);
        facility.add1MonthDefaultSchedules(createYearMonth);
    }

    public List<ScheduleResponse> find1MonthSchedules(Long facilityId,
        YearMonth findYearMonth) {
        Facility facility = findFacilityById(facilityId);
        List<Schedule> schedules = scheduleQueryRepository
            .find1MonthSchedules(facility, findYearMonth);

        return schedules.stream()
            .map(ScheduleResponse::of)
            .collect(Collectors.toList());
    }

    @Transactional
    public void delete1MonthSchedules(Long facilityId, YearMonth deleteYearMonth,
        String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Facility facility = findFacilityById(facilityId);
        facility.verifyManagementPermission(loginMember);

        scheduleQueryRepository.delete1MonthSchedules(facility, deleteYearMonth);
    }

    private Facility findFacilityById(Long facilityId) {
        return facilityRepository.findById(facilityId)
            .orElseThrow(() -> new NotFoundEntityException("시설", facilityId));
    }

    private Schedule findScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new NotFoundEntityException("시설스케줄", scheduleId));
    }
}
