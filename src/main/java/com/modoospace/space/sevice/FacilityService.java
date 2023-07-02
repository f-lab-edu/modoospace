package com.modoospace.space.sevice;

import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.space.controller.dto.FacilitySchedulesUpdateDto;
import com.modoospace.space.controller.dto.facility.FacilityCreateDto;
import com.modoospace.space.controller.dto.facility.FacilityReadDetailDto;
import com.modoospace.space.controller.dto.facility.FacilityUpdateDto;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FacilityService {

  private final MemberRepository memberRepository;
  private final SpaceRepository spaceRepository;
  private final FacilityRepository facilityRepository;

  @Transactional
  public Long createFacility(Long spaceId, FacilityCreateDto createDto, String loginEmail) {
    Member host = findMemberByEmail(loginEmail);
    Space space = findSpaceById(spaceId);
    space.verifyManagementPermission(host);

    Facility facility = createDto.toEntity(space);
    facilityRepository.save(facility);

    return facility.getId();
  }


  public FacilityReadDetailDto findFacility(Long facilityId) {
    Facility facility = findFacilityById(facilityId);

    return FacilityReadDetailDto.toDto(facility);
  }

  @Transactional
  public void updateFacility(Long facilityId, FacilityUpdateDto updateDto, String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    Facility facility = findFacilityById(facilityId);
    Facility updatedFacility = updateDto.toEntity();

    facility.update(updatedFacility, loginMember);
  }

  // 해당 메서드 제거 예정 (FacilitySchedule 서비스 생성예정)
  @Transactional
  public void updateFacilitySchedules(Long facilityId, FacilitySchedulesUpdateDto updateDto,
      String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    Facility facility = findFacilityById(facilityId);
    Facility updatedFacility = updateDto.toEntity();

    facility.updateSchedules(updatedFacility, loginMember);
  }

  @Transactional
  public void deleteFacility(Long facilityId, String email) {
    Member loginMember = findMemberByEmail(email);
    Facility facility = findFacilityById(facilityId);

    // TODO: 예약이 존재하는지 확인이 필요합니다.

    facility.verifyManagementPermission(loginMember);
    facilityRepository.delete(facility);
  }

  private Member findMemberByEmail(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEntityException("사용자", email));
    return member;
  }

  private Space findSpaceById(Long spaceId) {
    Space space = spaceRepository.findById(spaceId)
        .orElseThrow(() -> new NotFoundEntityException("공간", spaceId));
    return space;
  }

  private Facility findFacilityById(Long facilityId) {
    Facility facility = facilityRepository.findById(facilityId)
        .orElseThrow(() -> new NotFoundEntityException("시설", facilityId));
    return facility;
  }
}
