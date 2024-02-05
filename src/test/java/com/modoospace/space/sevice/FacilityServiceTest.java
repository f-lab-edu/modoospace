package com.modoospace.space.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.reservation.domain.DateTimeRange;
import com.modoospace.space.controller.dto.facility.FacilityCreateDto;
import com.modoospace.space.controller.dto.facility.FacilityUpdateDto;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateDto;
import com.modoospace.space.controller.dto.weekdaySetting.WeekdaySettingCreateDto;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import com.modoospace.space.repository.ScheduleQueryRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FacilityServiceTest {

    @Autowired
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
    private ScheduleQueryRepository scheduleQueryRepository;

    private Member hostMember;
    private Space space;
    private LocalDate now;

    @BeforeEach
    public void setUp() {
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

        now = LocalDate.now();
        if (now.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            now = now.plusDays(1);
        }
        if (now.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            now = now.plusDays(2);
        }
    }

    @DisplayName("시설 생성 시 Setting시간을 선택하지않으면 24시간 예약이 가능한 시설이 생성된다.")
    @Test
    public void createFacility_24HourOpen_ifNotSelectSetting() {
        FacilityCreateDto createDto = createFacility(true);

        Long facilityId = facilityService
            .createFacility(space.getId(), createDto, hostMember.getEmail());

        Facility facility = facilityRepository.findById(facilityId).get();

        assertThat(facility.getId()).isEqualTo(facilityId);
        assertThatFacilityInfo(facility, createDto);
        assertThat(scheduleQueryRepository.isIncludingSchedule(facility,
            new DateTimeRange(now, 0, now.plusDays(2), 24))).isTrue();
    }

    @DisplayName("시설 생성 시 시간, 요일 Setting에 맞게 예약이 가능한 시설이 생성된다.")
    @Test
    public void createFacility() {
        List<TimeSettingCreateDto> timeSettings = createTimeSetting(9, 21);
        List<WeekdaySettingCreateDto> weekdaySettings = createWeekDaySetting(DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        );
        FacilityCreateDto createDto = createFacilityWithSetting(false, timeSettings,
            weekdaySettings);

        Long facilityId = facilityService
            .createFacility(space.getId(), createDto, hostMember.getEmail());

        Facility facility = facilityRepository.findById(facilityId).get();
        assertThat(facility.getId()).isEqualTo(facilityId);
        assertThatFacilityInfo(facility, createDto);
        assertThat(scheduleQueryRepository.isIncludingSchedule(facility,
            new DateTimeRange(now, 9, now, 21))).isTrue();
    }

    @DisplayName("시설 정보를 업데이트한다.")
    @Test
    public void updateFacility() {
        FacilityCreateDto createDto = createFacility(false);
        Long facilityId = facilityService
            .createFacility(space.getId(), createDto, hostMember.getEmail());
        FacilityUpdateDto updateDto = FacilityUpdateDto.builder()
            .name("스터디룸2")
            .reservationEnable(true)
            .minUser(3)
            .maxUser(6)
            .description("3~6인실 입니다.")
            .build();

        facilityService
            .updateFacility(facilityId, updateDto, hostMember.getEmail());

        Facility facility = facilityRepository.findById(facilityId).get();
        assertThat(facility.getId()).isEqualTo(facilityId);
        assertThatFacilityInfo(facility, updateDto);
        assertThat(scheduleQueryRepository.isIncludingSchedule(facility,
            new DateTimeRange(now, 0, now.plusDays(2), 24))).isTrue();
    }

    @DisplayName("시설 정보와 세팅정보도 함께 업데이트한다.")
    @Test
    public void updateFacility_withSetting() {
        FacilityCreateDto createDto = createFacility(false);
        Long facilityId = facilityService
            .createFacility(space.getId(), createDto, hostMember.getEmail());
        List<TimeSettingCreateDto> timeSettings = createTimeSetting(9, 21);
        List<WeekdaySettingCreateDto> weekdaySettings = createWeekDaySetting(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        );
        FacilityUpdateDto updateDto = FacilityUpdateDto.builder()
            .name("스터디룸2")
            .reservationEnable(true)
            .minUser(3)
            .maxUser(6)
            .description("3~6인실 입니다.")
            .timeSettings(timeSettings)
            .weekdaySettings(weekdaySettings)
            .build();

        facilityService
            .updateFacility(facilityId, updateDto, hostMember.getEmail());

        Facility facility = facilityRepository.findById(facilityId).get();
        assertThat(facility.getId()).isEqualTo(facilityId);
        assertThatFacilityInfo(facility, updateDto);
        assertThat(scheduleQueryRepository.isIncludingSchedule(facility,
            new DateTimeRange(now, 9, now, 21))).isTrue();
    }

    private FacilityCreateDto createFacility(Boolean enable) {
        return FacilityCreateDto.builder()
            .name("스터디룸1")
            .reservationEnable(enable)
            .minUser(1)
            .maxUser(4)
            .description("1~4인실 입니다.")
            .build();
    }

    private FacilityCreateDto createFacilityWithSetting(Boolean enable,
        List<TimeSettingCreateDto> timeSettings,
        List<WeekdaySettingCreateDto> weekdaySettings) {
        return FacilityCreateDto.builder()
            .name("스터디룸1")
            .reservationEnable(enable)
            .minUser(1)
            .maxUser(4)
            .description("1~4인실 입니다.")
            .timeSettings(timeSettings)
            .weekdaySettings(weekdaySettings)
            .build();
    }

    private List<TimeSettingCreateDto> createTimeSetting(Integer start, Integer end) {
        return Arrays.asList(new TimeSettingCreateDto(start, end));
    }

    private List<WeekdaySettingCreateDto> createWeekDaySetting(DayOfWeek... dayOfWeeks) {
        return Arrays.stream(dayOfWeeks)
            .map(WeekdaySettingCreateDto::new)
            .collect(Collectors.toList());
    }

    private void assertThatFacilityInfo(Facility facility, FacilityCreateDto dto) {
        assertAll(
            () -> assertThat(facility.getName()).isEqualTo(dto.getName()),
            () -> assertThat(facility.getReservationEnable()).isEqualTo(
                dto.getReservationEnable()),
            () -> assertThat(facility.getMinUser()).isEqualTo(dto.getMinUser()),
            () -> assertThat(facility.getMaxUser()).isEqualTo(dto.getMaxUser()),
            () -> assertThat(facility.getDescription()).isEqualTo(dto.getDescription())
        );
    }

    private void assertThatFacilityInfo(Facility facility, FacilityUpdateDto dto) {
        assertAll(
            () -> assertThat(facility.getName()).isEqualTo(dto.getName()),
            () -> assertThat(facility.getReservationEnable()).isEqualTo(
                dto.getReservationEnable()),
            () -> assertThat(facility.getMinUser()).isEqualTo(dto.getMinUser()),
            () -> assertThat(facility.getMaxUser()).isEqualTo(dto.getMaxUser()),
            () -> assertThat(facility.getDescription()).isEqualTo(dto.getDescription())
        );
    }
}
