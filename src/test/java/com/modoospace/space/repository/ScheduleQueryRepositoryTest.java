package com.modoospace.space.repository;

import com.modoospace.JpaTestConfig;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.reservation.domain.DateTimeRange;
import com.modoospace.space.controller.dto.facility.FacilityCreateRequest;
import com.modoospace.space.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaTestConfig.class)
@ActiveProfiles("test")
public class ScheduleQueryRepositoryTest {

    @Autowired
    private ScheduleQueryRepository scheduleQueryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    private Facility roomFacility;

    private LocalDate now;
    private LocalDate tomorrow;

    @BeforeEach
    public void setUp() {
        Member hostMember = Member.builder()
                .email("host@email")
                .name("host")
                .role(Role.HOST)
                .build();
        memberRepository.save(hostMember);

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
                .build();
        roomFacility = facilityRepository.save(createRequest.toEntity(space));

        now = LocalDate.now();
        tomorrow = LocalDate.now().plusDays(1);
    }

    @DisplayName("해당 기간에 시설이 Open했는지 여부를 반환한다.")
    @Test
    public void isIncludingSchedule() {
        DateTimeRange dateTimeRange = new DateTimeRange(now, 9, now, 14);
        assertThat(scheduleQueryRepository
                .isIncludingSchedule(roomFacility, dateTimeRange)).isTrue();

        dateTimeRange = new DateTimeRange(now, 0, tomorrow, 24);
        assertThat(scheduleQueryRepository
                .isIncludingSchedule(roomFacility, dateTimeRange)).isTrue();
    }

    @DisplayName("해당 기간에 시설이 Open했는지 여부를 반환한다. (날짜마다 시간범위가 다른 case)")
    @Test
    public void isIncludingSchedule_ifScheduleUpdate() {
        // 오늘날짜 + 1 스케줄 데이터를 업데이트한다. (0~18로 변경)
        updateTomorrowSchedule(0, 18);

        DateTimeRange dateTimeRange = new DateTimeRange(now, 9, tomorrow, 18);
        assertThat(scheduleQueryRepository
                .isIncludingSchedule(roomFacility, dateTimeRange)).isTrue();

        dateTimeRange = new DateTimeRange(now, 9, tomorrow, 20);
        assertThat(scheduleQueryRepository
                .isIncludingSchedule(roomFacility, dateTimeRange)).isFalse();
    }

    @DisplayName("해당 기간에 시설이 Open했는지 여부를 반환한다. (한 날짜에 스케줄이 여러개 있는 case)")
    @Test
    public void isIncludingSchedule_ifScheduleUpdate2() {
        // 오늘날짜 + 1 스케줄 데이터를 업데이트한다. (9~12, 13~18로 변경)
        updateTomorrowSchedule(9, 12, 13, 18);

        DateTimeRange dateTimeRange = new DateTimeRange(now, 9, tomorrow, 18);
        assertThat(scheduleQueryRepository
                .isIncludingSchedule(roomFacility, dateTimeRange)).isFalse();

        dateTimeRange = new DateTimeRange(tomorrow, 13, tomorrow, 18);
        assertThat(scheduleQueryRepository
                .isIncludingSchedule(roomFacility, dateTimeRange)).isTrue();
    }

    @DisplayName("1달치 스케줄을 삭제한다.")
    @Test
    public void delete1MonthSchedules() {
        scheduleQueryRepository.delete1MonthSchedules(roomFacility, YearMonth.now());

        List<Schedule> retSchedules = scheduleQueryRepository.find1MonthSchedules(roomFacility, YearMonth.now());
        assertThat(retSchedules).isEmpty();
    }

    @DisplayName("해당 시설의 스케줄을 전부 삭제한다.")
    @Test
    public void deleteFacilitySchedules() {
        scheduleQueryRepository.deleteFacilitySchedules(roomFacility);

        assertThat(scheduleQueryRepository.find1MonthSchedules(roomFacility, YearMonth.now())).isEmpty();
        assertThat(scheduleQueryRepository.find1MonthSchedules(roomFacility, YearMonth.now().plusMonths(1))).isEmpty();
        assertThat(scheduleQueryRepository.find1MonthSchedules(roomFacility, YearMonth.now().plusMonths(2))).isEmpty();
    }

    private void updateTomorrowSchedule(Integer startHour, Integer endHour) {
        Schedule updateSchedule = createTomorrowSchedule(startHour, endHour);

        removeAndAddTomorrowSchedule(updateSchedule);
    }

    private void updateTomorrowSchedule(Integer startHour1, Integer endHour1,
                                        Integer startHour2, Integer endHour2) {
        Schedule updateSchedule1 = createTomorrowSchedule(startHour1, endHour1);
        Schedule updateSchedule2 = createTomorrowSchedule(startHour2, endHour2);

        removeAndAddTomorrowSchedule(updateSchedule1, updateSchedule2);
    }

    private Schedule createTomorrowSchedule(Integer startHour, Integer endHour) {
        TimeRange timeRange = new TimeRange(startHour, endHour);
        return Schedule.builder()
                .date(tomorrow)
                .timeRange(timeRange)
                .facility(roomFacility)
                .build();
    }

    private void removeAndAddTomorrowSchedule(Schedule... schedule) {
        List<Schedule> schedules = roomFacility.getSchedules().getSchedules();
        schedules.remove(now.getDayOfMonth());
        schedules.addAll(Arrays.asList(schedule));
        scheduleRepository.flush();
    }
}
