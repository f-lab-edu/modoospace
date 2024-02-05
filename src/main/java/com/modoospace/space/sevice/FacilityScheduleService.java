package com.modoospace.space.sevice;

import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.service.MemberService;
import com.modoospace.space.controller.dto.facilitySchedule.ScheduleReadDto;
import com.modoospace.space.controller.dto.facilitySchedule.ScheduleCreateUpdateDto;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.Schedule;
import com.modoospace.space.domain.ScheduleRepository;
import com.modoospace.space.domain.Schedules;
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
public class FacilityScheduleService {

    private final MemberService memberService;
    private final FacilityRepository facilityRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleQueryRepository scheduleQueryRepository;

    @Transactional
    public void createSchedule(Long facilityId, ScheduleCreateUpdateDto createDto,
        String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Facility facility = findFacilityById(facilityId);

        facility.addSchedule(createDto.toEntity(facility), loginMember);
    }


    public ScheduleReadDto findFacilitySchedule(Long facilityScheduleId) {
        Schedule schedule = findFacilityScheduleById(facilityScheduleId);

        return ScheduleReadDto.toDto(schedule);
    }

    @Transactional
    public void updateFacilitySchedule(Long facilityScheduleId, ScheduleCreateUpdateDto updateDto,
        String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Schedule schedule = findFacilityScheduleById(facilityScheduleId);
        Facility facility = schedule.getFacility();

        facility.updateSchedule(updateDto.toEntity(facility), schedule, loginMember);
    }

    @Transactional
    public void deleteFacilitySchedule(Long facilityScheduleId, String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Schedule schedule = findFacilityScheduleById(facilityScheduleId);
        Facility facility = schedule.getFacility();
        facility.verifyManagementPermission(loginMember);

        scheduleRepository.delete(schedule);
    }

    public List<ScheduleReadDto> find1DayFacilitySchedules(Long facilityId,
        LocalDate findDate) {
        Facility facility = findFacilityById(facilityId);
        List<Schedule> schedules = scheduleQueryRepository
            .find1DaySchedules(facility, findDate);

        return schedules.stream()
            .map(facilitySchedule -> ScheduleReadDto.toDto(facilitySchedule))
            .collect(Collectors.toList());
    }

    @Transactional
    public void create1MonthDefaultFacilitySchedules(Long facilityId, YearMonth createYearMonth,
        String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Facility facility = findFacilityById(facilityId);

        delete1MonthFacilitySchedules(facility, createYearMonth, loginMember);
        facility.create1MonthDefaultFacilitySchedules(createYearMonth, loginMember);
    }

    public List<ScheduleReadDto> find1MonthFacilitySchedules(Long facilityId,
        YearMonth findYearMonth) {
        Facility facility = findFacilityById(facilityId);
        List<Schedule> schedules = scheduleQueryRepository
            .find1MonthSchedules(facility, findYearMonth);

        return schedules.stream()
            .map(facilitySchedule -> ScheduleReadDto.toDto(facilitySchedule))
            .collect(Collectors.toList());
    }

    @Transactional
    public void delete1MonthFacilitySchedules(Long facilityId, YearMonth deleteYearMonth,
        String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Facility facility = findFacilityById(facilityId);

        delete1MonthFacilitySchedules(facility, deleteYearMonth, loginMember);
    }

    private void delete1MonthFacilitySchedules(Facility facility, YearMonth deleteYearMonth,
        Member loginMember) {
        facility.verifyManagementPermission(loginMember);

        List<Schedule> schedules = scheduleQueryRepository
            .find1MonthSchedules(facility, deleteYearMonth);
        if (!schedules.isEmpty()) {
            scheduleRepository
                .deleteAllInBatch(schedules); // deleteAll 과 deleteAllInBatch의 차이점 공부필요.
        }
    }

    private Facility findFacilityById(Long facilityId) {
        Facility facility = facilityRepository.findById(facilityId)
            .orElseThrow(() -> new NotFoundEntityException("시설", facilityId));
        return facility;
    }

    private Schedule findFacilityScheduleById(Long facilityScheduleId) {
        Schedule schedule = scheduleRepository.findById(facilityScheduleId)
            .orElseThrow(() -> new NotFoundEntityException("시설스케줄", facilityScheduleId));
        return schedule;
    }
}
