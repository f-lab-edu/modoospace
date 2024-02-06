package com.modoospace.space.sevice;

import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.service.MemberService;
import com.modoospace.space.controller.dto.schedule.ScheduleResponse;
import com.modoospace.space.controller.dto.schedule.ScheduleCreateUpdateRequest;
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

        facility.addSchedule(createRequest.toEntity(facility), loginMember);
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

        facility.updateSchedule(updateRequest.toEntity(facility), schedule, loginMember);
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
            .map(facilitySchedule -> ScheduleResponse.of(facilitySchedule))
            .collect(Collectors.toList());
    }

    @Transactional
    public void create1MonthDefaultSchedules(Long facilityId, YearMonth createYearMonth,
        String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Facility facility = findFacilityById(facilityId);

        delete1MonthSchedules(facility, createYearMonth, loginMember);
        facility.create1MonthDefaultSchedules(createYearMonth, loginMember);
    }

    private void delete1MonthSchedules(Facility facility, YearMonth deleteYearMonth,
        Member loginMember) {
        facility.verifyManagementPermission(loginMember);

        List<Schedule> schedules = scheduleQueryRepository
            .find1MonthSchedules(facility, deleteYearMonth);
        if (!schedules.isEmpty()) {
            scheduleRepository
                .deleteAllInBatch(schedules); // deleteAll 과 deleteAllInBatch의 차이점 공부필요.
        }
    }

    public List<ScheduleResponse> find1MonthSchedules(Long facilityId,
        YearMonth findYearMonth) {
        Facility facility = findFacilityById(facilityId);
        List<Schedule> schedules = scheduleQueryRepository
            .find1MonthSchedules(facility, findYearMonth);

        return schedules.stream()
            .map(facilitySchedule -> ScheduleResponse.of(facilitySchedule))
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
        Facility facility = facilityRepository.findById(facilityId)
            .orElseThrow(() -> new NotFoundEntityException("시설", facilityId));
        return facility;
    }

    private Schedule findScheduleById(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new NotFoundEntityException("시설스케줄", scheduleId));
        return schedule;
    }
}
