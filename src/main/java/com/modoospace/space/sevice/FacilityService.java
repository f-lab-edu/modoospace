package com.modoospace.space.sevice;

import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.service.MemberService;
import com.modoospace.space.controller.dto.facility.FacilityCreateRequest;
import com.modoospace.space.controller.dto.facility.FacilityDetailResponse;
import com.modoospace.space.controller.dto.facility.FacilityResponse;
import com.modoospace.space.controller.dto.facility.FacilitySearchRequest;
import com.modoospace.space.controller.dto.facility.FacilityUpdateRequest;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import com.modoospace.space.repository.FacilityQueryRepository;
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
    private final FacilityQueryRepository facilityQueryRepository;

    @Transactional
    public Long createFacility(Long spaceId, FacilityCreateRequest createRequest, String loginEmail) {
        Member host = memberService.findMemberByEmail(loginEmail);
        Space space = findSpaceById(spaceId);
        space.verifyManagementPermission(host);

        Facility facility = createRequest.toEntity(space);
        facilityRepository.save(facility);

        return facility.getId();
    }

    public Page<FacilityResponse> searchFacility(Long spaceId, FacilitySearchRequest searchRequest,
        Pageable pageable) {
        return facilityQueryRepository.searchFacility(spaceId, searchRequest, pageable);
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
        Facility updatedFacility = updateRequest.toEntity();

        facility.update(updatedFacility, loginMember);
    }

    @Transactional
    public void deleteFacility(Long facilityId, String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Facility facility = findFacilityById(facilityId);

        // TODO: 예약이 존재하는지 확인이 필요합니다.

        facility.verifyManagementPermission(loginMember);
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
