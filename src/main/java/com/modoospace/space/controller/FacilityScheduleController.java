package com.modoospace.space.controller;

import com.modoospace.common.DateFormatManager;
import com.modoospace.config.auth.LoginEmail;
import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleCreateUpdateDto;
import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleReadDto;
import com.modoospace.space.sevice.FacilityScheduleService;
import java.net.URI;
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
public class FacilityScheduleController {

  private final FacilityScheduleService facilityScheduleService;

  @PostMapping()
  public ResponseEntity<Void> create(@PathVariable Long facilityId,
      @RequestBody @Valid FacilityScheduleCreateUpdateDto createDto,
      @LoginEmail String loginEmail) {
    Long scheduleId = facilityScheduleService
        .createFacilitySchedule(facilityId, createDto, loginEmail);
    return ResponseEntity
        .created(URI.create("/api/v1/facilities/" + facilityId + "/schedules/" + scheduleId))
        .build();
  }

  @GetMapping("/{scheduleId}")
  public ResponseEntity<FacilityScheduleReadDto> find(@PathVariable Long scheduleId) {
    FacilityScheduleReadDto facilityScheduleReadDto = facilityScheduleService
        .findFacilitySchedule(scheduleId);
    return ResponseEntity.ok().body(facilityScheduleReadDto);
  }

  @PutMapping("/{scheduleId}")
  public ResponseEntity<Void> update(@PathVariable Long scheduleId,
      @RequestBody @Valid FacilityScheduleCreateUpdateDto updateDto,
      @LoginEmail String loginEmail) {
    facilityScheduleService.updateFacilitySchedule(scheduleId, updateDto, loginEmail);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{scheduleId}")
  public ResponseEntity<Void> delete(@PathVariable Long scheduleId,
      @LoginEmail String loginEmail) {
    facilityScheduleService.deleteFacilitySchedule(scheduleId, loginEmail);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/day")
  public ResponseEntity<List<FacilityScheduleReadDto>> find1Day(@PathVariable Long facilityId,
      @RequestParam @DateTimeFormat(pattern = DateFormatManager.DATE_FORMAT) final LocalDate date) {
    List<FacilityScheduleReadDto> facilityScheduleReadDtos = facilityScheduleService
        .find1DayFacilitySchedules(facilityId, date);
    return ResponseEntity.ok().body(facilityScheduleReadDtos);
  }

  @PostMapping("/month")
  public ResponseEntity<Void> create1MonthDefault(@PathVariable Long facilityId,
      @RequestParam @DateTimeFormat(pattern = DateFormatManager.YEARMONTH_FORMAT) final YearMonth yearMonth,
      @LoginEmail String loginEmail) {
    facilityScheduleService
        .create1MonthDefaultFacilitySchedules(facilityId, yearMonth, loginEmail);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/month")
  public ResponseEntity<List<FacilityScheduleReadDto>> find1Month(@PathVariable Long facilityId,
      @RequestParam @DateTimeFormat(pattern = DateFormatManager.YEARMONTH_FORMAT) final YearMonth yearMonth) {
    List<FacilityScheduleReadDto> facilityScheduleReadDtos = facilityScheduleService
        .find1MonthFacilitySchedules(facilityId, yearMonth);
    return ResponseEntity.ok().body(facilityScheduleReadDtos);
  }

  @DeleteMapping("/month")
  public ResponseEntity<Void> delete1Month(@PathVariable Long facilityId,
      @RequestParam @DateTimeFormat(pattern = DateFormatManager.YEARMONTH_FORMAT) final YearMonth yearMonth,
      @LoginEmail String loginEmail) {
    facilityScheduleService
        .delete1MonthFacilitySchedules(facilityId, yearMonth, loginEmail);
    return ResponseEntity.noContent().build();
  }
}
