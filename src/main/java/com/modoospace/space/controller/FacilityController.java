package com.modoospace.space.controller;

import com.modoospace.config.auth.aop.CheckLogin;
import com.modoospace.config.auth.resolver.LoginMember;
import com.modoospace.member.domain.Member;
import com.modoospace.space.controller.dto.facility.*;
import com.modoospace.space.sevice.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/spaces/{spaceId}/facilities")
public class FacilityController {

    private final FacilityService facilityService;

    @CheckLogin
    @PostMapping()
    public ResponseEntity<Void> create(@PathVariable Long spaceId,
                                       @RequestBody @Valid FacilityCreateRequest createRequest,
                                       @LoginMember Member loginMember) {
        Long facilityId = facilityService.createFacility(spaceId, createRequest, loginMember);
        return ResponseEntity
                .created(URI.create("/api/v1/spaces/" + spaceId + "/facilities/" + facilityId)).build();
    }

    @GetMapping()
    public ResponseEntity<Page<FacilityResponse>> search(@PathVariable Long spaceId,
                                                         FacilitySearchRequest searchRequest, Pageable pageable) {
        Page<FacilityResponse> facilityResponses = facilityService
                .searchFacility(spaceId, searchRequest, pageable);
        return ResponseEntity.ok().body(facilityResponses);
    }

    @GetMapping("/{facilityId}")
    public ResponseEntity<FacilityResponse> find(@PathVariable Long facilityId) {
        FacilityResponse facilityReadDto = facilityService.findFacility(facilityId);
        return ResponseEntity.ok().body(facilityReadDto);
    }

    @CheckLogin
    @PutMapping("/{facilityId}")
    public ResponseEntity<Void> update(@PathVariable Long facilityId,
                                       @RequestBody @Valid FacilityUpdateRequest updateRequest,
                                       @LoginMember Member loginMember) {
        facilityService.updateFacility(facilityId, updateRequest, loginMember);
        return ResponseEntity.noContent().build();
    }

    @CheckLogin
    @PutMapping("/{facilityId}/setting")
    public ResponseEntity<Void> updateSetting(@PathVariable Long facilityId,
                                              @RequestBody @Valid FacilitySettingUpdateRequest updateRequest,
                                              @LoginMember Member loginMember) {
        facilityService.updateFacilitySetting(facilityId, updateRequest, loginMember);
        return ResponseEntity.noContent().build();
    }

    @CheckLogin
    @DeleteMapping("/{facilityId}")
    public ResponseEntity<Void> delete(@PathVariable Long facilityId,
                                       @LoginMember Member loginMember) {
        facilityService.deleteFacility(facilityId, loginMember);
        return ResponseEntity.noContent().build();
    }
}
