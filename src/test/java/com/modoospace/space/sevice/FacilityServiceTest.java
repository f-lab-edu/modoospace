package com.modoospace.space.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.TestConfig;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.facility.FacilityCreateDto;
import com.modoospace.space.controller.dto.facility.FacilityUpdateDto;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateDto;
import com.modoospace.space.controller.dto.weekdaySetting.WeekdaySettingCreateDto;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.FacilityType;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import com.modoospace.space.repository.FacilityScheduleQueryRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({TestConfig.class})
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class FacilityServiceTest {

  private FacilityService facilityService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private SpaceRepository spaceRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private FacilityScheduleQueryRepository facilityScheduleQueryRepository;

  private Member hostMember;
  private Space space;
  private LocalDate nowDate;

  @BeforeEach
  public void setUp() {
    facilityService = new FacilityService(memberRepository, spaceRepository, facilityRepository);

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

    nowDate = LocalDate.now();
    if (nowDate.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
      nowDate = nowDate.plusDays(1);
    }
    if (nowDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
      nowDate = nowDate.plusDays(2);
    }
  }

  @DisplayName("시설 생성 시 Setting시간을 선택하지않으면 24시간 예약이 가능한 시설이 생성된다.")
  @Test
  public void createFacility_24HourOpen_ifNotSelectSetting() {
    FacilityCreateDto createDto = FacilityCreateDto.builder()
        .name("스터디룸1")
        .facilityType(FacilityType.ROOM)
        .description("1~4인실 입니다.")
        .reservationEnable(true)
        .build();

    Long facilityId = facilityService
        .createFacility(space.getId(), createDto, hostMember.getEmail());
    Facility facility = facilityRepository.findById(facilityId).get();

    LocalDateTime start = nowDate.atTime(0, 0, 0);
    LocalDateTime end = nowDate.plusDays(2).atTime(23, 59, 59);
    assertAll(
        () -> assertThat(facility.getId()).isEqualTo(facilityId),
        () -> assertThat(facility.getName()).isEqualTo("스터디룸1"),
        () -> assertThat(facility.getFacilityType()).isEqualTo(FacilityType.ROOM),
        () -> assertThat(facility.getDescription()).isEqualTo("1~4인실 입니다."),
        () -> assertThat(facility.getReservationEnable()).isTrue(),
        () -> assertThat(facilityScheduleQueryRepository.isIncludingSchedule(facility, start, end))
            .isTrue()
    );
  }

  @DisplayName("시설 생성 시 시간, 요일 Setting에 맞게 예약이 가능한 시설이 생성된다.")
  @Test
  public void createFacility() {
    List<TimeSettingCreateDto> timeSettings = Arrays
        .asList(new TimeSettingCreateDto(LocalTime.of(9, 0, 0), LocalTime.of(20, 59, 59)));
    List<WeekdaySettingCreateDto> weekdaySettings = Arrays.asList(
        new WeekdaySettingCreateDto(DayOfWeek.MONDAY),
        new WeekdaySettingCreateDto(DayOfWeek.TUESDAY),
        new WeekdaySettingCreateDto(DayOfWeek.WEDNESDAY),
        new WeekdaySettingCreateDto(DayOfWeek.THURSDAY),
        new WeekdaySettingCreateDto(DayOfWeek.FRIDAY)
    );
    FacilityCreateDto createDto = FacilityCreateDto.builder()
        .name("스터디룸1")
        .facilityType(FacilityType.ROOM)
        .description("1~4인실 입니다.")
        .reservationEnable(false)
        .timeSettings(timeSettings)
        .weekdaySettings(weekdaySettings)
        .build();

    Long facilityId = facilityService
        .createFacility(space.getId(), createDto, hostMember.getEmail());
    Facility facility = facilityRepository.findById(facilityId).get();

    LocalDateTime start = nowDate.atTime(9, 0, 0);
    LocalDateTime end = nowDate.atTime(20, 59, 59);
    assertAll(
        () -> assertThat(facility.getId()).isEqualTo(facilityId),
        () -> assertThat(facility.getName()).isEqualTo("스터디룸1"),
        () -> assertThat(facility.getFacilityType()).isEqualTo(FacilityType.ROOM),
        () -> assertThat(facility.getDescription()).isEqualTo("1~4인실 입니다."),
        () -> assertThat(facility.getReservationEnable()).isFalse(),
        () -> assertThat(facilityScheduleQueryRepository.isIncludingSchedule(facility, start, end))
            .isTrue()
    );
  }

  @DisplayName("시설 정보를 업데이트한다.")
  @Test
  public void updateFacility() {
    FacilityCreateDto createDto = FacilityCreateDto.builder()
        .name("스터디룸1")
        .facilityType(FacilityType.ROOM)
        .reservationEnable(false)
        .description("1~4인실 입니다.")
        .build();
    Long facilityId = facilityService
        .createFacility(space.getId(), createDto, hostMember.getEmail());
    FacilityUpdateDto updateDto = FacilityUpdateDto.builder()
        .name("스터디룸업데이트")
        .description("설명업데이트")
        .reservationEnable(true)
        .build();

    facilityService
        .updateFacility(facilityId, updateDto, hostMember.getEmail());
    Facility facility = facilityRepository.findById(facilityId).get();

    LocalDateTime start = nowDate.atTime(0, 0, 0);
    LocalDateTime end = nowDate.plusDays(2).atTime(23, 59, 59);
    assertAll(
        () -> assertThat(facility.getId()).isEqualTo(facilityId),
        () -> assertThat(facility.getName()).isEqualTo("스터디룸업데이트"),
        () -> assertThat(facility.getFacilityType()).isEqualTo(FacilityType.ROOM),
        () -> assertThat(facility.getDescription()).isEqualTo("설명업데이트"),
        () -> assertThat(facility.getReservationEnable()).isTrue(),
        () -> assertThat(facilityScheduleQueryRepository.isIncludingSchedule(facility, start, end))
            .isTrue()
    );
  }

  @DisplayName("시설 정보와 세팅정보도 함께 업데이트한다.")
  @Test
  public void updateFacility_withSetting() {
    FacilityCreateDto createDto = FacilityCreateDto.builder()
        .name("스터디룸1")
        .facilityType(FacilityType.ROOM)
        .reservationEnable(false)
        .description("1~4인실 입니다.")
        .build();
    Long facilityId = facilityService
        .createFacility(space.getId(), createDto, hostMember.getEmail());
    List<TimeSettingCreateDto> timeSettings = Arrays
        .asList(new TimeSettingCreateDto(LocalTime.of(9, 0, 0), LocalTime.of(20, 59, 59)));
    List<WeekdaySettingCreateDto> weekdaySettings = Arrays.asList(
        new WeekdaySettingCreateDto(DayOfWeek.MONDAY),
        new WeekdaySettingCreateDto(DayOfWeek.TUESDAY),
        new WeekdaySettingCreateDto(DayOfWeek.WEDNESDAY),
        new WeekdaySettingCreateDto(DayOfWeek.THURSDAY),
        new WeekdaySettingCreateDto(DayOfWeek.FRIDAY)
    );
    FacilityUpdateDto updateDto = FacilityUpdateDto.builder()
        .name("스터디룸업데이트")
        .description("설명업데이트")
        .reservationEnable(true)
        .timeSettings(timeSettings)
        .weekdaySettings(weekdaySettings)
        .build();

    facilityService
        .updateFacility(facilityId, updateDto, hostMember.getEmail());
    Facility facility = facilityRepository.findById(facilityId).get();

    LocalDateTime start = nowDate.atTime(9, 0, 0);
    LocalDateTime end = nowDate.atTime(20, 59, 59);
    assertAll(
        () -> assertThat(facility.getId()).isEqualTo(facilityId),
        () -> assertThat(facility.getName()).isEqualTo("스터디룸업데이트"),
        () -> assertThat(facility.getFacilityType()).isEqualTo(FacilityType.ROOM),
        () -> assertThat(facility.getDescription()).isEqualTo("설명업데이트"),
        () -> assertThat(facility.getReservationEnable()).isTrue(),
        () -> assertThat(facilityScheduleQueryRepository.isIncludingSchedule(facility, start, end))
            .isTrue(),
        () -> assertThat(
            facilityScheduleQueryRepository.isIncludingSchedule(facility, start, end.plusDays(1)))
            .isFalse()
    );
  }
}
