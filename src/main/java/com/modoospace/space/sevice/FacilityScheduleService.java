package com.modoospace.space.sevice;

import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleCreateUpdateDto;
import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleReadDto;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.FacilitySchedule;
import com.modoospace.space.domain.FacilityScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FacilityScheduleService {

  private final FacilityScheduleRepository facilityScheduleRepository;
  private final FacilityRepository facilityRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public void createFacilitySchedule(Long facilityId, FacilityScheduleCreateUpdateDto createDto,
      String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    Facility facility = findFacilityById(facilityId);

    FacilitySchedule createSchedule = createDto.toEntity(facility);
    facility.addFacilitySchedule(createSchedule, loginMember);
  }

  public FacilityScheduleReadDto findFacilitySchedule(Long facilityScheduleId) {
    FacilitySchedule schedule = findFacilityScheduleById(facilityScheduleId);

    return FacilityScheduleReadDto.toDto(schedule);
  }

  public List<FacilityScheduleReadDto> findFacilityScheduleByLocalDate(Long facilityId, LocalDate date) {
    Facility facility = findFacilityById(facilityId);
    // TODO : QueryDSL 개선 필요
    List<FacilitySchedule> facilitySchedules = facility.getFacilitySchedules().isEqualsLocalDate(date, date);

    return facilitySchedules.stream()
        .map(facilitySchedule -> FacilityScheduleReadDto.toDto(facilitySchedule))
        .collect(Collectors.toList());
  }

  @Transactional
  public void updateFacilitySchedule(Long facilityScheduleId,
      FacilityScheduleCreateUpdateDto updateDto,
      String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    FacilitySchedule schedule = findFacilityScheduleById(facilityScheduleId);
    Facility facility = schedule.getFacility();

    FacilitySchedule updateSchedule = updateDto.toEntity(facility);
    facility.updateFacilitySchedule(updateSchedule, schedule, loginMember);
  }

  @Transactional
  public void deleteFacilitySchedule(Long facilityScheduleId, String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    FacilitySchedule schedule = findFacilityScheduleById(facilityScheduleId);
    Facility facility = schedule.getFacility();

    facility.verifyManagementPermission(loginMember);
    facilityScheduleRepository.delete(schedule);
  }

  private Member findMemberByEmail(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEntityException("사용자", email));
    return member;
  }

  private Facility findFacilityById(Long facilityId) {
    Facility facility = facilityRepository.findById(facilityId)
        .orElseThrow(() -> new NotFoundEntityException("시설", facilityId));
    return facility;
  }

  private FacilitySchedule findFacilityScheduleById(Long facilityScheduleId) {
    FacilitySchedule facilitySchedule = facilityScheduleRepository.findById(facilityScheduleId)
        .orElseThrow(() -> new NotFoundEntityException("시설스케줄", facilityScheduleId));
    return facilitySchedule;
  }
}
