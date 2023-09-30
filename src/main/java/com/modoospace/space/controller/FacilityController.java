package com.modoospace.space.controller;

import com.modoospace.config.auth.LoginEmail;
import com.modoospace.space.controller.dto.facility.FacilityCreateDto;
import com.modoospace.space.controller.dto.facility.FacilityReadDetailDto;
import com.modoospace.space.controller.dto.facility.FacilityReadDto;
import com.modoospace.space.controller.dto.facility.FacilitySearchDto;
import com.modoospace.space.controller.dto.facility.FacilityUpdateDto;
import com.modoospace.space.sevice.FacilityService;
import java.net.URI;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/v1/spaces/{spaceId}/facilities")
public class FacilityController {

  private final FacilityService facilityService;

  @PostMapping()
  public ResponseEntity<Void> create(@PathVariable Long spaceId,
      @RequestBody @Valid FacilityCreateDto createDto,
      @LoginEmail String loginEmail) {
    Long facilityId = facilityService.createFacility(spaceId, createDto, loginEmail);
    return ResponseEntity
        .created(URI.create("/api/v1/spaces/" + spaceId + "/facilities/" + facilityId)).build();
  }

  @GetMapping()
  public ResponseEntity<Page<FacilityReadDto>> search(@PathVariable Long spaceId,
      FacilitySearchDto searchDto, Pageable pageable) {
    Page<FacilityReadDto> facilityReadDtos = facilityService
        .searchFacility(spaceId, searchDto, pageable);
    return ResponseEntity.ok().body(facilityReadDtos);
  }

  @GetMapping("/{facilityId}")
  public ResponseEntity<FacilityReadDetailDto> find(@PathVariable Long facilityId) {
    FacilityReadDetailDto facilityReadDto = facilityService.findFacility(facilityId);
    return ResponseEntity.ok().body(facilityReadDto);
  }

  @PutMapping("/{facilityId}")
  public ResponseEntity<Void> update(@PathVariable Long facilityId,
      @RequestBody @Valid FacilityUpdateDto updateDto,
      @LoginEmail String loginEmail) {
    facilityService.updateFacility(facilityId, updateDto, loginEmail);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{facilityId}")
  public ResponseEntity<Void> delete(@PathVariable Long facilityId,
      @LoginEmail String loginEmail) {
    facilityService.deleteFacility(facilityId, loginEmail);
    return ResponseEntity.noContent().build();
  }
}
