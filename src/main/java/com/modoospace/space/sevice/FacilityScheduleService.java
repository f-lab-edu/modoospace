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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FacilityScheduleService {

  private final MemberRepository memberRepository;
  private final FacilityRepository facilityRepository;
  private final FacilityScheduleRepository facilityScheduleRepository;

  @Transactional
  public Long createFacilitySchedule(Long facilityId, FacilityScheduleCreateUpdateDto createDto,
      String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    Facility facility = findFacilityById(facilityId);

    FacilitySchedule facilitySchedule = facility
        .addFacilitySchedule(createDto.toEntity(facility), loginMember);
    facilityScheduleRepository.flush();

    return facilitySchedule.getId();
  }

  public FacilityScheduleReadDto findFacilitySchedule(Long facilityScheduleId) {
    FacilitySchedule schedule = findFacilityScheduleById(facilityScheduleId);

    return FacilityScheduleReadDto.toDto(schedule);
  }

  @Transactional
  public Long updateFacilitySchedule(Long facilityScheduleId,
      FacilityScheduleCreateUpdateDto updateDto,
      String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    FacilitySchedule schedule = findFacilityScheduleById(facilityScheduleId);
    Facility facility = schedule.getFacility();

    FacilitySchedule facilitySchedule = facility
        .updateFacilitySchedule(updateDto.toEntity(facility), schedule, loginMember);
    facilityScheduleRepository.flush();

    return facilitySchedule.getId();
  }

  @Transactional
  public void deleteFacilitySchedule(Long facilityScheduleId, String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    FacilitySchedule schedule = findFacilityScheduleById(facilityScheduleId);
    Facility facility = schedule.getFacility();

    facility.verifyManagementPermission(loginMember);
    facilityScheduleRepository.delete(schedule);
  }

  public List<FacilityScheduleReadDto> find1DayFacilitySchedules(Long facilityId,
      LocalDate findDate) {
    Facility facility = findFacilityById(facilityId);
    List<FacilitySchedule> facilitySchedules = find1DayFacilitySchedules(facility, findDate);

    return facilitySchedules.stream()
        .map(facilitySchedule -> FacilityScheduleReadDto.toDto(facilitySchedule))
        .collect(Collectors.toList());
  }

  @Transactional
  public void create1MonthDefaultFacilitySchedules(Long facilityId, YearMonth createYearMonth,
      String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    Facility facility = findFacilityById(facilityId);

    delete1MonthFacilitySchedules(facility, createYearMonth, loginMember);
    facility.create1MonthDefaultFacilitySchedules(createYearMonth, loginMember);
  }

  public List<FacilityScheduleReadDto> find1MonthFacilitySchedules(Long facilityId,
      YearMonth findYearMonth) {
    Facility facility = findFacilityById(facilityId);
    List<FacilitySchedule> facilitySchedules = find1MonthFacilitySchedules(facility, findYearMonth);

    return facilitySchedules.stream()
        .map(facilitySchedule -> FacilityScheduleReadDto.toDto(facilitySchedule))
        .collect(Collectors.toList());
  }

  @Transactional
  public void delete1MonthFacilitySchedules(Long facilityId, YearMonth deleteYearMonth,
      String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    Facility facility = findFacilityById(facilityId);

    delete1MonthFacilitySchedules(facility, deleteYearMonth, loginMember);
  }

  private void delete1MonthFacilitySchedules(Facility facility, YearMonth deleteYearMonth,
      Member loginMember) {
    facility.verifyManagementPermission(loginMember);

    List<FacilitySchedule> facilitySchedules = find1MonthFacilitySchedules(facility,
        deleteYearMonth);
    if (!facilitySchedules.isEmpty()) {
      facilityScheduleRepository
          .deleteAllInBatch(facilitySchedules); // deleteAll 과 deleteAllInBatch의 차이점 공부필요.
    }
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

  private List<FacilitySchedule> find1DayFacilitySchedules(Facility facility,
      LocalDate findDate) {
    LocalDateTime startDateTime = findDate.atTime(0, 0, 0);
    LocalDateTime endDateTime = findDate.atTime(23, 59, 59);

    return facilityScheduleRepository
        .findByFacilityAndStartDateTimeBetweenOrderByStartDateTime(facility, startDateTime,
            endDateTime);
  }

  private List<FacilitySchedule> find1MonthFacilitySchedules(Facility facility,
      YearMonth findYearMonth) {
    LocalDateTime startDateTime = findYearMonth.atDay(1).atTime(0, 0, 0);
    LocalDateTime endDateTime = findYearMonth.atEndOfMonth().atTime(23, 59, 59);

    return facilityScheduleRepository
        .findByFacilityAndStartDateTimeBetweenOrderByStartDateTime(facility, startDateTime,
            endDateTime);
  }
}
