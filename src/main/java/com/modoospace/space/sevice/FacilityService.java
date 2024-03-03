package com.modoospace.space.sevice;

import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.service.MemberService;
import com.modoospace.space.controller.dto.facility.*;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import com.modoospace.space.repository.FacilityQueryRepository;
import com.modoospace.space.repository.ScheduleQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FacilityService {

    private final MemberService memberService;
    private final SpaceRepository spaceRepository;
    private final FacilityRepository facilityRepository;
    private final ScheduleQueryRepository scheduleQueryRepository;
    private final FacilityQueryRepository facilityQueryRepository;

    @Transactional
    public Long createFacility(Long spaceId, FacilityCreateRequest createRequest,
                               String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Space space = findSpaceById(spaceId);
        space.verifyManagementPermission(loginMember);

        Facility facility = createRequest.toEntity(space);
        facilityRepository.save(facility);

        return facility.getId();
    }

    public Page<FacilityResponse> searchFacility(Long spaceId, FacilitySearchRequest searchRequest,
                                                 Pageable pageable) {
        Page<Facility> facilities = facilityQueryRepository.searchFacility(spaceId, searchRequest, pageable);

        return facilities.map(FacilityResponse::of);
    }

    public FacilityDetailResponse findFacility(Long facilityId) {
        Facility facility = findFacilityById(facilityId);

        return FacilityDetailResponse.of(facility);
    }

    @Transactional
    public void updateFacility(Long facilityId, FacilityUpdateRequest updateRequest,
                               String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Facility facility = findFacilityById(facilityId);
        facility.verifyManagementPermission(loginMember);

        Facility updatedFacility = updateRequest.toEntity();
        facility.update(updatedFacility);
    }

    @Transactional
    public void updateFacilitySetting(Long facilityId, FacilitySettingUpdateRequest updateRequest,
                                      String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Facility facility = findFacilityById(facilityId);
        facility.verifyManagementPermission(loginMember);

        scheduleQueryRepository.deleteFacilitySchedules(facility);
        facility.updateSetting(updateRequest.toTimeSettings(), updateRequest.toWeekdaySettings());
    }

    @Transactional
    public void deleteFacility(Long facilityId, String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Facility facility = findFacilityById(facilityId);
        facility.verifyManagementPermission(loginMember);

        scheduleQueryRepository.deleteFacilitySchedules(facility);
        facilityRepository.delete(facility);
    }

    private Space findSpaceById(Long spaceId) {
        return spaceRepository.findById(spaceId)
                .orElseThrow(() -> new NotFoundEntityException("공간", spaceId));
    }

    private Facility findFacilityById(Long facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new NotFoundEntityException("시설", facilityId));
    }
}
