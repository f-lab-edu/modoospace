package com.modoospace.space.controller;

import com.modoospace.common.DateFormatManager;
import com.modoospace.config.auth.aop.CheckLogin;
import com.modoospace.config.auth.resolver.LoginMember;
import com.modoospace.member.domain.Member;
import com.modoospace.space.controller.dto.schedule.ScheduleCreateUpdateRequest;
import com.modoospace.space.controller.dto.schedule.ScheduleResponse;
import com.modoospace.space.sevice.ScheduleService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/facilities/{facilityId}/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @CheckLogin
    @PostMapping()
    public ResponseEntity<Void> create(@PathVariable Long facilityId,
        @RequestBody @Valid ScheduleCreateUpdateRequest createRequest,
        @LoginMember Member loginMember) {
        scheduleService.createSchedule(facilityId, createRequest, loginMember);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> find(@PathVariable Long scheduleId) {
        ScheduleResponse schedule = scheduleService.findSchedule(scheduleId);
        return ResponseEntity.ok().body(schedule);
    }

    @CheckLogin
    @PutMapping("/{scheduleId}")
    public ResponseEntity<Void> update(@PathVariable Long scheduleId,
        @RequestBody @Valid ScheduleCreateUpdateRequest updateRequest,
                                       @LoginMember Member loginMember) {
        scheduleService.updateSchedule(scheduleId, updateRequest, loginMember);
        return ResponseEntity.noContent().build();
    }

    @CheckLogin
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> delete(@PathVariable Long scheduleId,
                                       @LoginMember Member loginMember) {
        scheduleService.deleteSchedule(scheduleId, loginMember);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/day")
    public ResponseEntity<List<ScheduleResponse>> find1Day(@PathVariable Long facilityId,
        @RequestParam @DateTimeFormat(pattern = DateFormatManager.DATE_FORMAT) final LocalDate date) {
        List<ScheduleResponse> schedules = scheduleService
            .find1DaySchedules(facilityId, date);
        return ResponseEntity.ok().body(schedules);
    }

    @CheckLogin
    @PostMapping("/month")
    public ResponseEntity<Void> create1MonthDefault(@PathVariable Long facilityId,
        @RequestParam @DateTimeFormat(pattern = DateFormatManager.YEARMONTH_FORMAT) final YearMonth yearMonth,
                                                    @LoginMember Member loginMember) {
        scheduleService.create1MonthDefaultSchedules(facilityId, yearMonth, loginMember);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/month")
    public ResponseEntity<List<ScheduleResponse>> find1Month(@PathVariable Long facilityId,
        @RequestParam @DateTimeFormat(pattern = DateFormatManager.YEARMONTH_FORMAT) final YearMonth yearMonth) {
        List<ScheduleResponse> schedules = scheduleService
            .find1MonthSchedules(facilityId, yearMonth);
        return ResponseEntity.ok().body(schedules);
    }

    @CheckLogin
    @DeleteMapping("/month")
    public ResponseEntity<Void> delete1Month(@PathVariable Long facilityId,
        @RequestParam @DateTimeFormat(pattern = DateFormatManager.YEARMONTH_FORMAT) final YearMonth yearMonth,
        @LoginMember Member loginMember) {
        scheduleService
            .delete1MonthSchedules(facilityId, yearMonth, loginMember);
        return ResponseEntity.noContent().build();
    }
}
