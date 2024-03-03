package com.modoospace.space.sevice;

import com.modoospace.AbstractIntegrationContainerBaseTest;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.reservation.domain.DateTimeRange;
import com.modoospace.space.controller.dto.facility.FacilityCreateRequest;
import com.modoospace.space.controller.dto.facility.FacilitySettingUpdateRequest;
import com.modoospace.space.controller.dto.facility.FacilityUpdateRequest;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateRequest;
import com.modoospace.space.controller.dto.weekdaySetting.WeekdaySettingCreateRequest;
import com.modoospace.space.domain.*;
import com.modoospace.space.repository.ScheduleQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
class FacilityServiceTest extends AbstractIntegrationContainerBaseTest {

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

    private LocalDate workingDay;

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

        workingDay = LocalDate.now();
        if (workingDay.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            workingDay = workingDay.plusDays(1);
        }
        if (workingDay.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            workingDay = workingDay.plusDays(2);
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
                new DateTimeRange(workingDay, 0, workingDay.plusDays(2), 24))).isTrue();
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

    private void assertThatFacilityInfo(Facility facility, FacilityCreateRequest request) {
        assertAll(
                () -> assertThat(facility.getName()).isEqualTo(request.getName()),
                () -> assertThat(facility.getReservationEnable()).isEqualTo(
                        request.getReservationEnable()),
                () -> assertThat(facility.getMinUser()).isEqualTo(request.getMinUser()),
                () -> assertThat(facility.getMaxUser()).isEqualTo(request.getMaxUser()),
                () -> assertThat(facility.getDescription()).isEqualTo(request.getDescription())
        );
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
                new DateTimeRange(workingDay, 9, workingDay, 21))).isTrue();
    }

    private List<TimeSettingCreateRequest> createTimeSetting(Integer start, Integer end) {
        return List.of(new TimeSettingCreateRequest(start, end));
    }

    private List<WeekdaySettingCreateRequest> createWeekDaySetting(DayOfWeek... dayOfWeeks) {
        return Arrays.stream(dayOfWeeks)
                .map(WeekdaySettingCreateRequest::new)
                .collect(Collectors.toList());
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
        assertThatFacilityInfo(facility, updateRequest);
    }

    private void assertThatFacilityInfo(Facility facility, FacilityUpdateRequest request) {
        assertAll(
                () -> assertThat(facility.getName()).isEqualTo(request.getName()),
                () -> assertThat(facility.getReservationEnable()).isEqualTo(
                        request.getReservationEnable()),
                () -> assertThat(facility.getMinUser()).isEqualTo(request.getMinUser()),
                () -> assertThat(facility.getMaxUser()).isEqualTo(request.getMaxUser()),
                () -> assertThat(facility.getDescription()).isEqualTo(request.getDescription())
        );
    }

    @DisplayName("시설시간 및 요일 세팅을 업데이트한다.")
    @Test
    public void updateFacilitySetting() {
        // 1. 데이터 생성 Transaction(Commit)
        Long facilityId = facilityService
                .createFacility(space.getId(), createFacility(true), hostMember.getEmail());
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // 2. 시설 업데이트 및 검증 Transaction(Rollback)
        TestTransaction.start();
        updateFacilitySetting(facilityId);
        assertThatSchedules(facilityId);
        TestTransaction.end();

        // 3. 생성된 데이터 삭제 Transaction(Commit)
        TestTransaction.start();
        deleteAll();
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    private void updateFacilitySetting(Long facilityId) {
        FacilitySettingUpdateRequest updateRequest = new FacilitySettingUpdateRequest(
                createTimeSetting(9, 21),
                createWeekDaySetting(
                        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY)
        );
        facilityService.updateFacilitySetting(facilityId, updateRequest, hostMember.getEmail());
    }

    private void assertThatSchedules(Long facilityId) {
        Facility facility = facilityRepository.findById(facilityId).get();
        assertThat(scheduleQueryRepository.isIncludingSchedule(facility,
                new DateTimeRange(workingDay, 0, workingDay, 24))).isFalse();
    }

    private void deleteAll() {
        facilityRepository.deleteAll();
        spaceRepository.deleteAll();
        categoryRepository.deleteAll();
        memberRepository.deleteAll();
    }
}
