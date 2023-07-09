package com.modoospace.space.controller;

import com.modoospace.config.auth.LoginEmail;
import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleCreateUpdateDto;
import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleReadDto;
import com.modoospace.space.sevice.FacilityScheduleService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    facilityScheduleService.createFacilitySchedule(facilityId, createDto, loginEmail);
    return ResponseEntity.noContent().build();
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
}
