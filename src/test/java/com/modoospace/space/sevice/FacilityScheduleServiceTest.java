package com.modoospace.space.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.exception.ConflictingTimeException;
import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.facility.FacilityCreateDto;
import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleCreateUpdateDto;
import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleReadDto;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateDto;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.FacilitySchedule;
import com.modoospace.space.domain.FacilityScheduleRepository;
import com.modoospace.space.domain.FacilityType;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class FacilityScheduleServiceTest {

  private FacilityScheduleService facilityScheduleService;

  @Autowired
  private FacilityScheduleRepository facilityScheduleRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private SpaceRepository spaceRepository;

  private Member hostMember;

  private Space space;

  private Facility facility;

  private LocalDate nowDate;

  @BeforeEach
  public void setUp() {
    facilityScheduleService = new FacilityScheduleService(facilityScheduleRepository,
        facilityRepository, memberRepository);

    hostMember = Member.builder()
        .email("host@email")
        .name("host")
        .role(Role.HOST)
        .build();
    memberRepository.save(hostMember);

    Category category = Category.builder()
        .name("스터디 공간")
        .build();
    categoryRepository.save(category);
    SpaceCreateUpdateDto spaceCreateDto = SpaceCreateUpdateDto.builder()
        .name("공간이름")
        .description("설명")
        .build();
    space = spaceCreateDto.toEntity(category, hostMember);
    spaceRepository.save(space);

    FacilityCreateDto createDto = FacilityCreateDto.builder()
        .name("스터디룸1")
        .facilityType(FacilityType.ROOM)
        .description("1~4인실 입니다.")
        .reservationEnable(true)
        .timeSettings(Arrays
            .asList(new TimeSettingCreateDto(LocalTime.of(9, 0, 0), LocalTime.of(17, 59, 59))))
        .build();
    facility = createDto.toEntity(space);
    facilityRepository.save(facility);

    nowDate = LocalDate.now();
  }

  @DisplayName("시설 스케줄 데이터를 생성한다.")
  @Test
  public void createFacilitySchedule() {
    FacilityScheduleCreateUpdateDto createDto = new FacilityScheduleCreateUpdateDto(
        LocalDateTime.of(nowDate, LocalTime.of(19, 0, 0)),
        (LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59))));

    facilityScheduleService
        .createFacilitySchedule(facility.getId(), createDto, hostMember.getEmail());

    List<FacilitySchedule> facilitySchedules = facility.getFacilitySchedules()
        .isEqualsLocalDate(nowDate, nowDate);
    assertAll(
        () -> assertThat(facilitySchedules.get(0).isStartTimeEquals(LocalTime.of(9, 0, 0))),
        () -> assertThat(facilitySchedules.get(0).isEndTimeEquals(LocalTime.of(17, 59, 59))),
        () -> assertThat(facilitySchedules.get(1).isStartTimeEquals(LocalTime.of(19, 0, 0))),
        () -> assertThat(facilitySchedules.get(1).isEndTimeEquals(LocalTime.of(23, 59, 59)))
    );
  }

  @DisplayName("시설 스케줄 데이터를 생성한다. 하지만 범위가 연속적이므로 합쳐서 저장된다.")
  @Test
  public void createFacilitySchedule_merge() {
    FacilityScheduleCreateUpdateDto createDto = new FacilityScheduleCreateUpdateDto(
        LocalDateTime.of(nowDate, LocalTime.of(18, 0, 0)),
        (LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59))));

    facilityScheduleService
        .createFacilitySchedule(facility.getId(), createDto, hostMember.getEmail());

    List<FacilitySchedule> facilitySchedules = facility.getFacilitySchedules()
        .isEqualsLocalDate(nowDate, nowDate);
    assertAll(
        () -> assertThat(facilitySchedules.get(0).isStartTimeEquals(LocalTime.of(9, 0, 0))),
        () -> assertThat(facilitySchedules.get(0).isEndTimeEquals(LocalTime.of(23, 59, 59)))
    );
  }

  @DisplayName("시설 스케줄을 생성 시 기존 스케줄과 겹친다면 예외를 던진다.")
  @Test
  public void createFacilitySchedule_throwException_ifConflict() {
    FacilityScheduleCreateUpdateDto createDto = new FacilityScheduleCreateUpdateDto(
        LocalDateTime.of(nowDate, LocalTime.of(16, 0, 0)),
        (LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59))));

    assertThatThrownBy(() -> facilityScheduleService
        .createFacilitySchedule(facility.getId(), createDto, hostMember.getEmail()))
        .isInstanceOf(ConflictingTimeException.class);
  }

  @DisplayName("시설 스케줄을 업데이트 한다.")
  @Test
  public void updateFacilitySchedule() {
    FacilityScheduleCreateUpdateDto updateDto = new FacilityScheduleCreateUpdateDto(
        LocalDateTime.of(nowDate, LocalTime.of(0, 0, 0)),
        (LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59))));
    List<FacilitySchedule> facilitySchedules = facility.getFacilitySchedules()
        .isEqualsLocalDate(nowDate, nowDate);
    FacilitySchedule targetSchedule = facilitySchedules.get(0);

    facilityScheduleService
        .updateFacilitySchedule(targetSchedule.getId(), updateDto, hostMember.getEmail());

    FacilitySchedule retSchedule = facilityScheduleRepository.findById(targetSchedule.getId())
        .get();
    assertAll(
        () -> assertThat(retSchedule.isStartTimeEquals(LocalTime.of(0, 0, 0))),
        () -> assertThat(retSchedule.isEndTimeEquals(LocalTime.of(23, 59, 59)))
    );
  }

  @DisplayName("시설 스케줄을 업데이트 한다. 하지만 범위가 연속적이므로 합쳐서 저장된다.")
  @Test
  public void updateFacilitySchedule_merge() {
    FacilityScheduleCreateUpdateDto createDto = new FacilityScheduleCreateUpdateDto(
        LocalDateTime.of(nowDate, LocalTime.of(20, 0, 0)),
        (LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59))));
    facilityScheduleService
        .createFacilitySchedule(facility.getId(), createDto, hostMember.getEmail());
    facilityRepository.flush(); // flush 하지 않으면 id가 null임. 왜일까?
    FacilityScheduleCreateUpdateDto updateDto = new FacilityScheduleCreateUpdateDto(
        LocalDateTime.of(nowDate, LocalTime.of(18, 0, 0)),
        (LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59))));
    FacilityScheduleReadDto targetSchedule = facilityScheduleService
        .findFacilityScheduleByLocalDate(facility.getId(), nowDate).get(1);

    facilityScheduleService
        .updateFacilitySchedule(targetSchedule.getId(), updateDto, hostMember.getEmail());

    List<FacilitySchedule> retSchedules = facility.getFacilitySchedules()
        .isEqualsLocalDate(nowDate, nowDate);
    assertAll(
        () -> assertThat(retSchedules.get(0).isStartTimeEquals(LocalTime.of(0, 0, 0))),
        () -> assertThat(retSchedules.get(0).isEndTimeEquals(LocalTime.of(23, 59, 59)))
    );
  }

  @DisplayName("시설 스케줄을 업데이트 시 기존 스케줄과 겹친다면 예외를 던진다.")
  @Test
  public void updateFacilitySchedule_throwException_ifConflict() {
    FacilityScheduleCreateUpdateDto createDto = new FacilityScheduleCreateUpdateDto(
        LocalDateTime.of(nowDate, LocalTime.of(20, 0, 0)),
        (LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59))));
    facilityScheduleService
        .createFacilitySchedule(facility.getId(), createDto, hostMember.getEmail());
    facilityRepository.flush(); // flush 하지 않으면 id가 null임. 왜일까?
    FacilityScheduleCreateUpdateDto updateDto = new FacilityScheduleCreateUpdateDto(
        LocalDateTime.of(nowDate, LocalTime.of(16, 0, 0)),
        (LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59))));
    FacilityScheduleReadDto facilityScheduleReadDto = facilityScheduleService
        .findFacilityScheduleByLocalDate(facility.getId(), nowDate).get(1);

    assertThatThrownBy(() -> facilityScheduleService
        .updateFacilitySchedule(facilityScheduleReadDto.getId(), updateDto, hostMember.getEmail()))
        .isInstanceOf(ConflictingTimeException.class);
  }

  @DisplayName("시설 스케줄을 삭제한다.")
  @Test
  public void deleteFacilitySchedule() {
    FacilityScheduleReadDto targetSchedule = facilityScheduleService
        .findFacilityScheduleByLocalDate(facility.getId(), nowDate).get(0);

    facilityScheduleService.deleteFacilitySchedule(targetSchedule.getId(), hostMember.getEmail());

    assertThatThrownBy(() -> facilityScheduleService.findFacilitySchedule(targetSchedule.getId()))
        .isInstanceOf(NotFoundEntityException.class);
  }
}
