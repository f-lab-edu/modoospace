package com.modoospace.space.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.reservation.domain.DateTimeRange;
import com.modoospace.space.controller.dto.facility.FacilityCreateRequest;
import com.modoospace.space.controller.dto.facility.FacilityUpdateRequest;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateRequest;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateRequest;
import com.modoospace.space.controller.dto.weekdaySetting.WeekdaySettingCreateRequest;
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

        Category category = new Category("스터디 공간");
        categoryRepository.save(category);

        space = Space.builder()
            .name("공간이름")
            .description("설명")
            .category(category)
            .host(hostMember)
            .build();
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
        FacilityCreateRequest createRequest = createFacility(true);

        Long facilityId = facilityService
            .createFacility(space.getId(), createRequest, hostMember.getEmail());

        Facility facility = facilityRepository.findById(facilityId).get();

        assertThat(facility.getId()).isEqualTo(facilityId);
        assertThatFacilityInfo(facility, createRequest);
        assertThat(scheduleQueryRepository.isIncludingSchedule(facility,
            new DateTimeRange(now, 0, now.plusDays(2), 24))).isTrue();
    }

    @DisplayName("시설 생성 시 시간, 요일 Setting에 맞게 예약이 가능한 시설이 생성된다.")
    @Test
    public void createFacility() {
        List<TimeSettingCreateRequest> timeSettings = createTimeSetting(9, 21);
        List<WeekdaySettingCreateRequest> weekdaySettings = createWeekDaySetting(DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        );
        FacilityCreateRequest createRequest = createFacilityWithSetting(false, timeSettings,
            weekdaySettings);

        Long facilityId = facilityService
            .createFacility(space.getId(), createRequest, hostMember.getEmail());

        Facility facility = facilityRepository.findById(facilityId).get();
        assertThat(facility.getId()).isEqualTo(facilityId);
        assertThatFacilityInfo(facility, createRequest);
        assertThat(scheduleQueryRepository.isIncludingSchedule(facility,
            new DateTimeRange(now, 9, now, 21))).isTrue();
    }

    @DisplayName("시설 정보를 업데이트한다.")
    @Test
    public void updateFacility() {
        FacilityCreateRequest createRequest = createFacility(false);
        Long facilityId = facilityService
            .createFacility(space.getId(), createRequest, hostMember.getEmail());
        FacilityUpdateRequest updateRequest = FacilityUpdateRequest.builder()
            .name("스터디룸2")
            .reservationEnable(true)
            .minUser(3)
            .maxUser(6)
            .description("3~6인실 입니다.")
            .build();

        facilityService
            .updateFacility(facilityId, updateRequest, hostMember.getEmail());

        Facility facility = facilityRepository.findById(facilityId).get();
        assertThat(facility.getId()).isEqualTo(facilityId);
        assertThatFacilityInfo(facility, updateRequest);
        assertThat(scheduleQueryRepository.isIncludingSchedule(facility,
            new DateTimeRange(now, 0, now.plusDays(2), 24))).isTrue();
    }

    @DisplayName("시설 정보와 세팅정보도 함께 업데이트한다.")
    @Test
    public void updateFacility_withSetting() {
        FacilityCreateRequest createRequest = createFacility(false);
        Long facilityId = facilityService
            .createFacility(space.getId(), createRequest, hostMember.getEmail());
        List<TimeSettingCreateRequest> timeSettings = createTimeSetting(9, 21);
        List<WeekdaySettingCreateRequest> weekdaySettings = createWeekDaySetting(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        );
        FacilityUpdateRequest updateRequest = FacilityUpdateRequest.builder()
            .name("스터디룸2")
            .reservationEnable(true)
            .minUser(3)
            .maxUser(6)
            .description("3~6인실 입니다.")
            .timeSettings(timeSettings)
            .weekdaySettings(weekdaySettings)
            .build();

        facilityService
            .updateFacility(facilityId, updateRequest, hostMember.getEmail());

        Facility facility = facilityRepository.findById(facilityId).get();
        assertThat(facility.getId()).isEqualTo(facilityId);
        assertThatFacilityInfo(facility, updateRequest);
        assertThat(scheduleQueryRepository.isIncludingSchedule(facility,
            new DateTimeRange(now, 9, now, 21))).isTrue();
    }

    private FacilityCreateRequest createFacility(Boolean enable) {
        return FacilityCreateRequest.builder()
            .name("스터디룸1")
            .reservationEnable(enable)
            .minUser(1)
            .maxUser(4)
            .description("1~4인실 입니다.")
            .build();
    }

    private FacilityCreateRequest createFacilityWithSetting(Boolean enable,
        List<TimeSettingCreateRequest> timeSettings,
        List<WeekdaySettingCreateRequest> weekdaySettings) {
        return FacilityCreateRequest.builder()
            .name("스터디룸1")
            .reservationEnable(enable)
            .minUser(1)
            .maxUser(4)
            .description("1~4인실 입니다.")
            .timeSettings(timeSettings)
            .weekdaySettings(weekdaySettings)
            .build();
    }

    private List<TimeSettingCreateRequest> createTimeSetting(Integer start, Integer end) {
        return Arrays.asList(new TimeSettingCreateRequest(start, end));
    }

    private List<WeekdaySettingCreateRequest> createWeekDaySetting(DayOfWeek... dayOfWeeks) {
        return Arrays.stream(dayOfWeeks)
            .map(WeekdaySettingCreateRequest::new)
            .collect(Collectors.toList());
    }

    private void assertThatFacilityInfo(Facility facility, FacilityCreateRequest dto) {
        assertAll(
            () -> assertThat(facility.getName()).isEqualTo(dto.getName()),
            () -> assertThat(facility.getReservationEnable()).isEqualTo(
                dto.getReservationEnable()),
            () -> assertThat(facility.getMinUser()).isEqualTo(dto.getMinUser()),
            () -> assertThat(facility.getMaxUser()).isEqualTo(dto.getMaxUser()),
            () -> assertThat(facility.getDescription()).isEqualTo(dto.getDescription())
        );
    }

    private void assertThatFacilityInfo(Facility facility, FacilityUpdateRequest dto) {
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
