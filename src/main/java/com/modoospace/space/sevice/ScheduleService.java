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
import javax.persistence.EntityManager;
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
    private final EntityManager em;

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
            .map(ScheduleResponse::of)
            .collect(Collectors.toList());
    }

    @Transactional
    public void create1MonthDefaultSchedules(Long facilityId, YearMonth createYearMonth,
        String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Facility facility = findFacilityById(facilityId);

        // 문제 : 쿼리를 직접날려 삭제를 실행했기때문에, 영속성컨테이너의 facility의 스케줄데이터는 그대로.
        delete1MonthSchedules(facility, createYearMonth, loginMember);
        // 해결 : 영속성 컨텍스트를 비운 후 엔티티를 다시 조회하여 데이터 일관성을 보장.
        // 다른방식 : 엔티티의 데이터를 코드로 직접 삭제해줘도 된다.
        em.clear();
        facility = findFacilityById(facilityId);
        facility.create1MonthDefaultSchedules(createYearMonth, loginMember);
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

        delete1MonthSchedules(facility, deleteYearMonth, loginMember);
    }

    private void delete1MonthSchedules(Facility facility, YearMonth deleteYearMonth,
        Member loginMember) {
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
