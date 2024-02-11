package com.modoospace.space.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.common.exception.ConflictingTimeException;
import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.facility.FacilityCreateRequest;
import com.modoospace.space.controller.dto.schedule.ScheduleCreateUpdateRequest;
import com.modoospace.space.controller.dto.schedule.ScheduleResponse;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateRequest;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ScheduleServiceTest {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    private Member hostMember;

    private Facility facility;

    private LocalDate nowDate;

    private YearMonth nowYearMonth;

    @BeforeEach
    public void setUp() {
        hostMember = Member.builder()
            .email("host@email")
            .name("host")
            .role(Role.HOST)
            .build();
        memberRepository.save(hostMember);
        memberRepository.flush();

        Category category = new Category("스터디 공간");
        categoryRepository.save(category);

        Space space = Space.builder()
            .name("공간이름")
            .description("설명")
            .category(category)
            .host(hostMember)
            .build();
        spaceRepository.save(space);

        // TimeSetting, WeekSetting 기본값이 필요하여 Request 사용.
        FacilityCreateRequest createRequest = FacilityCreateRequest.builder()
            .name("스터디룸1")
            .reservationEnable(true)
            .minUser(1)
            .maxUser(4)
            .description("1~4인실 입니다.")
            .timeSettings(List.of(new TimeSettingCreateRequest(9, 18)))
            .build();
        facility = facilityRepository.save(createRequest.toEntity(space));

        nowDate = LocalDate.now();
        nowYearMonth = YearMonth.now();
    }

    @DisplayName("시설 스케줄 데이터를 생성한다.")
    @Test
    public void createFacilitySchedule() {
        ScheduleCreateUpdateRequest createRequest = new ScheduleCreateUpdateRequest(nowDate, 19,
            24);

        scheduleService.createSchedule(
            facility.getId(), createRequest, hostMember.getEmail());

        ScheduleResponse retSchedule = scheduleService.find1DaySchedules(
            facility.getId(), nowDate).get(1);
        assertAll(
            () -> assertThat(retSchedule.getStartHour()).isEqualTo(19),
            () -> assertThat(retSchedule.getEndHour()).isEqualTo(24)
        );
    }

    @DisplayName("시설 스케줄 데이터를 생성한다. 하지만 범위가 연속적이므로 합쳐서 저장된다.")
    @Test
    public void createFacilitySchedule_merge() {
        ScheduleCreateUpdateRequest createRequest = new ScheduleCreateUpdateRequest(nowDate, 18,
            24);

        scheduleService.createSchedule(
            facility.getId(), createRequest, hostMember.getEmail());

        ScheduleResponse retSchedule = scheduleService.find1DaySchedules(
            facility.getId(), nowDate).get(0);
        assertAll(
            () -> assertThat(retSchedule.getStartHour()).isEqualTo(9),
            () -> assertThat(retSchedule.getEndHour()).isEqualTo(24)
        );
    }

    @DisplayName("시설 스케줄을 생성 시 기존 스케줄과 겹친다면 예외를 던진다.")
    @Test
    public void createFacilitySchedule_throwException_ifConflict() {
        ScheduleCreateUpdateRequest createRequest = new ScheduleCreateUpdateRequest(
            nowDate, 16, 24);

        assertThatThrownBy(() -> scheduleService
            .createSchedule(facility.getId(), createRequest, hostMember.getEmail()))
            .isInstanceOf(ConflictingTimeException.class);
    }

    @DisplayName("시설 스케줄을 업데이트 한다.")
    @Test
    public void updateFacilitySchedule() {
        ScheduleCreateUpdateRequest updateRequest = new ScheduleCreateUpdateRequest(
            nowDate, 0, 24);
        ScheduleResponse targetSchedule = scheduleService.find1DaySchedules(
            facility.getId(), nowDate).get(0);

        scheduleService
            .updateSchedule(targetSchedule.getId(), updateRequest, hostMember.getEmail());

        ScheduleResponse retSchedule = scheduleService.find1DaySchedules(
            facility.getId(), nowDate).get(0);
        assertAll(
            () -> assertThat(retSchedule.getStartHour()).isEqualTo(0),
            () -> assertThat(retSchedule.getEndHour()).isEqualTo(24)
        );
    }

    @DisplayName("시설 스케줄을 업데이트 한다. 하지만 범위가 연속적이므로 합쳐서 저장된다.")
    @Test
    public void updateFacilitySchedule_merge() {
        ScheduleCreateUpdateRequest createRequest = new ScheduleCreateUpdateRequest(
            nowDate, 20, 24);
        scheduleService.createSchedule(
            facility.getId(), createRequest, hostMember.getEmail());
        ScheduleResponse createSchedule = scheduleService.find1DaySchedules(
            facility.getId(), nowDate).get(1);
        ScheduleCreateUpdateRequest updateRequest = new ScheduleCreateUpdateRequest(
            nowDate, 18, 24);

        scheduleService.updateSchedule(
            createSchedule.getId(), updateRequest, hostMember.getEmail());

        ScheduleResponse retSchedule = scheduleService.find1DaySchedules(
            facility.getId(), nowDate).get(0);
        assertAll(
            () -> assertThat(retSchedule.getStartHour()).isEqualTo(9),
            () -> assertThat(retSchedule.getEndHour()).isEqualTo(24)
        );
    }

    @DisplayName("시설 스케줄을 업데이트 시 기존 스케줄과 겹친다면 예외를 던진다.")
    @Test
    public void updateFacilitySchedule_throwException_ifConflict() {
        ScheduleCreateUpdateRequest createRequest = new ScheduleCreateUpdateRequest(
            nowDate, 20, 24);
        scheduleService.createSchedule(
            facility.getId(), createRequest, hostMember.getEmail());
        ScheduleCreateUpdateRequest updateRequest = new ScheduleCreateUpdateRequest(
            nowDate, 16, 24);
        ScheduleResponse createSchedule = scheduleService.find1DaySchedules(
            facility.getId(), nowDate).get(1);

        assertThatThrownBy(() -> scheduleService.updateSchedule(
            createSchedule.getId(), updateRequest, hostMember.getEmail()))
            .isInstanceOf(ConflictingTimeException.class);
    }

    @DisplayName("시설 스케줄을 삭제한다.")
    @Test
    public void deleteFacilitySchedule() {
        ScheduleResponse targetSchedule = scheduleService
            .find1DaySchedules(facility.getId(), nowDate).get(0);

        scheduleService.deleteSchedule(
            targetSchedule.getId(), hostMember.getEmail());

        assertThatThrownBy(
            () -> scheduleService.findSchedule(targetSchedule.getId()))
            .isInstanceOf(NotFoundEntityException.class);
    }

    @DisplayName("하루 치 스케줄데이터를 조회한다.")
    @Test
    public void find1DayFacilitySchedules() {
        List<ScheduleResponse> facilitySchedules = scheduleService.find1DaySchedules(
            facility.getId(), nowDate);

        assertThat(facilitySchedules).hasSize(1);
    }

    @DisplayName("1달 치 기본 스케줄을 생성한다.")
    @Test
    public void create1MonthDefaultFacilitySchedules_plus3Month() {
        YearMonth createYearMonth = nowYearMonth.plusMonths(3);

        scheduleService.create1MonthDefaultSchedules(
            facility.getId(), createYearMonth, hostMember.getEmail());

        List<ScheduleResponse> retReponses = scheduleService.find1MonthSchedules(
            facility.getId(), createYearMonth);
        assertAll(
            () -> assertThat(retReponses.size()).isEqualTo(createYearMonth.lengthOfMonth()),
            () -> assertThat(retReponses.get(0).getDate())
                .isEqualTo(createYearMonth.atDay(1)),
            () -> assertThat(retReponses.get(retReponses.size() - 1).getDate())
                .isEqualTo(createYearMonth.atEndOfMonth())
        );
    }

    @DisplayName("1달 치 스케줄데이터를 조회한다.")
    @Test
    public void find1MonthFacilitySchedules() {
        List<ScheduleResponse> facilitySchedules = scheduleService.find1MonthSchedules(
            facility.getId(), nowYearMonth);

        assertThat(facilitySchedules).hasSize(nowYearMonth.lengthOfMonth());
    }

    @DisplayName("이미 생성되어있는 1달 치 스케줄을 지우고 새로 생성한다.")
    @Test
    public void create1MonthDefaultFacilitySchedules_plus2Month() {
        // 1. 데이터 생성 Transaction(Commit)
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // 2. 스케줄 생성 및 검증 Transaction(Rollback)
        TestTransaction.start();
        YearMonth createYearMonth = nowYearMonth.plusMonths(2);
        scheduleService.create1MonthDefaultSchedules(
            facility.getId(), createYearMonth, hostMember.getEmail());
        assertAllSchedules(createYearMonth);
        TestTransaction.end();

        // 3. 생성된 데이터 삭제 Transaction(Commit)
        TestTransaction.start();
        deleteAll();
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    private void assertAllSchedules(YearMonth createYearMonth) {
        List<ScheduleResponse> retResponses = scheduleService.find1MonthSchedules(
            facility.getId(), createYearMonth);
        assertAll(
            () -> assertThat(retResponses.size()).isEqualTo(createYearMonth.lengthOfMonth()),
            () -> assertThat(retResponses.get(0).getDate())
                .isEqualTo(createYearMonth.atDay(1)),
            () -> assertThat(
                retResponses.get(retResponses.size() - 1).getDate())
                .isEqualTo(createYearMonth.atEndOfMonth())
        );
    }

    private void deleteAll() {
        facilityRepository.deleteAllInBatch();
        spaceRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("1달 치 스케줄을 삭제한다.")
    @Test
    public void delete1MonthFacilitySchedules() {
        YearMonth deleteYearMonth = nowYearMonth.plusMonths(2);

        scheduleService.delete1MonthSchedules(
            facility.getId(), deleteYearMonth, hostMember.getEmail());

        List<ScheduleResponse> retResponses = scheduleService
            .find1MonthSchedules(facility.getId(), deleteYearMonth);
        assertThat(retResponses).isEmpty();
    }
}
