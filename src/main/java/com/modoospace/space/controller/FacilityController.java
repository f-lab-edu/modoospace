package com.modoospace.space.controller;

import com.modoospace.config.auth.resolver.LoginMember;
import com.modoospace.member.domain.Member;
import com.modoospace.space.controller.dto.facility.FacilityCreateRequest;
import com.modoospace.space.controller.dto.facility.FacilityDetailResponse;
import com.modoospace.space.controller.dto.facility.FacilityResponse;
import com.modoospace.space.controller.dto.facility.FacilitySearchRequest;
import com.modoospace.space.controller.dto.facility.FacilitySettingUpdateRequest;
import com.modoospace.space.controller.dto.facility.FacilityUpdateRequest;
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
            @RequestBody @Valid FacilityCreateRequest createRequest,
            @LoginMember Member loginMember) {
        Long facilityId = facilityService.createFacility(spaceId, createRequest, loginMember);
        return ResponseEntity
                .created(URI.create("/api/v1/spaces/" + spaceId + "/facilities/" + facilityId))
                .build();
    }

    @GetMapping()
    public ResponseEntity<Page<FacilityResponse>> search(@PathVariable Long spaceId,
            FacilitySearchRequest searchRequest, Pageable pageable) {
        Page<FacilityResponse> facilities = facilityService
                .searchFacility(spaceId, searchRequest, pageable);
        return ResponseEntity.ok().body(facilities);
    }

    @GetMapping("/{facilityId}")
    public ResponseEntity<FacilityDetailResponse> find(@PathVariable Long facilityId) {
        FacilityDetailResponse facility = facilityService.findFacility(facilityId);
        return ResponseEntity.ok().body(facility);
    }

    @PutMapping("/{facilityId}")
    public ResponseEntity<Void> update(@PathVariable Long facilityId,
            @RequestBody @Valid FacilityUpdateRequest updateRequest,
            @LoginMember Member loginMember) {
        facilityService.updateFacility(facilityId, updateRequest, loginMember);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{facilityId}/setting")
    public ResponseEntity<Void> updateSetting(@PathVariable Long facilityId,
            @RequestBody @Valid FacilitySettingUpdateRequest updateRequest,
            @LoginMember Member loginMember) {
        facilityService.updateFacilitySetting(facilityId, updateRequest, loginMember);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{facilityId}")
    public ResponseEntity<Void> delete(@PathVariable Long facilityId,
            @LoginMember Member loginMember) {
        facilityService.deleteFacility(facilityId, loginMember);
        return ResponseEntity.noContent().build();
    }
}
