package com.modoospace.space.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.common.exception.ConflictingTimeException;
import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.facility.FacilityCreateDto;
import com.modoospace.space.controller.dto.facilitySchedule.ScheduleCreateUpdateDto;
import com.modoospace.space.controller.dto.facilitySchedule.ScheduleReadDto;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateDto;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
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
public class ScheduleServiceTest {

    @Autowired
    private FacilityScheduleService facilityScheduleService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private EntityManager em;

    private Member hostMember;

    private Space space;

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
            .reservationEnable(true)
            .minUser(1)
            .maxUser(4)
            .description("1~4인실 입니다.")
            .timeSettings(Arrays.asList(new TimeSettingCreateDto(9, 18)))
            .build();
        facility = createDto.toEntity(space);
        facilityRepository.save(facility);

        nowDate = LocalDate.now();
        nowYearMonth = YearMonth.now();

        em.flush();
    }

    @DisplayName("시설 스케줄 데이터를 생성한다.")
    @Test
    public void createFacilitySchedule() {
        ScheduleCreateUpdateDto createDto = new ScheduleCreateUpdateDto(nowDate, 19, 24);

        facilityScheduleService
            .createSchedule(facility.getId(), createDto, hostMember.getEmail());

        List<ScheduleReadDto> retSchedules = facilityScheduleService.find1DayFacilitySchedules(
            facility.getId(), nowDate);
        assertAll(
            () -> assertThat(retReadDto.getStartHour()).isEqualTo(19),
            () -> assertThat(retReadDto.getEndHour()).isEqualTo(24)
        );
    }

    @DisplayName("시설 스케줄 데이터를 생성한다. 하지만 범위가 연속적이므로 합쳐서 저장된다.")
    @Test
    public void createFacilitySchedule_merge() {
        ScheduleCreateUpdateDto createDto = new ScheduleCreateUpdateDto(nowDate, 18, 24);

        Long createdId = facilityScheduleService
            .createSchedule(facility.getId(), createDto, hostMember.getEmail());

        ScheduleReadDto retReadDto = facilityScheduleService
            .findFacilitySchedule(createdId);
        assertAll(
            () -> assertThat(retReadDto.getStartHour()).isEqualTo(9),
            () -> assertThat(retReadDto.getEndHour()).isEqualTo(24)
        );
    }

    @DisplayName("시설 스케줄을 생성 시 기존 스케줄과 겹친다면 예외를 던진다.")
    @Test
    public void createFacilitySchedule_throwException_ifConflict() {
        ScheduleCreateUpdateDto createDto = new ScheduleCreateUpdateDto(nowDate, 16, 24);

        assertThatThrownBy(() -> facilityScheduleService
            .createSchedule(facility.getId(), createDto, hostMember.getEmail()))
            .isInstanceOf(ConflictingTimeException.class);
    }

    @DisplayName("시설 스케줄을 업데이트 한다.")
    @Test
    public void updateFacilitySchedule() {
        ScheduleCreateUpdateDto updateDto = new ScheduleCreateUpdateDto(nowDate, 0, 24);
        ScheduleReadDto targetSchedule = facilityScheduleService
            .find1DayFacilitySchedules(facility.getId(), nowDate).get(0);

        Long updatedId = facilityScheduleService
            .updateFacilitySchedule(targetSchedule.getId(), updateDto, hostMember.getEmail());

        ScheduleReadDto retReadDto = facilityScheduleService
            .findFacilitySchedule(updatedId);
        assertAll(
            () -> assertThat(retReadDto.getStartHour()).isEqualTo(0),
            () -> assertThat(retReadDto.getEndHour()).isEqualTo(24)
        );
    }

    @DisplayName("시설 스케줄을 업데이트 한다. 하지만 범위가 연속적이므로 합쳐서 저장된다.")
    @Test
    public void updateFacilitySchedule_merge() {
        ScheduleCreateUpdateDto createDto = new ScheduleCreateUpdateDto(nowDate, 20, 24);
        Long createdId = facilityScheduleService
            .createSchedule(facility.getId(), createDto, hostMember.getEmail());
        ScheduleCreateUpdateDto updateDto = new ScheduleCreateUpdateDto(nowDate, 18, 24);

        Long updatedId = facilityScheduleService
            .updateFacilitySchedule(createdId, updateDto, hostMember.getEmail());

        ScheduleReadDto retReadDto = facilityScheduleService
            .findFacilitySchedule(updatedId);
        assertAll(
            () -> assertThat(retReadDto.getStartHour()).isEqualTo(9),
            () -> assertThat(retReadDto.getEndHour()).isEqualTo(24)
        );
    }

    @DisplayName("시설 스케줄을 업데이트 시 기존 스케줄과 겹친다면 예외를 던진다.")
    @Test
    public void updateFacilitySchedule_throwException_ifConflict() {
        ScheduleCreateUpdateDto createDto = new ScheduleCreateUpdateDto(nowDate, 20, 24);
        Long createdId = facilityScheduleService
            .createSchedule(facility.getId(), createDto, hostMember.getEmail());
        ScheduleCreateUpdateDto updateDto = new ScheduleCreateUpdateDto(nowDate, 16, 24);

        assertThatThrownBy(() -> facilityScheduleService
            .updateFacilitySchedule(createdId, updateDto, hostMember.getEmail()))
            .isInstanceOf(ConflictingTimeException.class);
    }

    @DisplayName("시설 스케줄을 삭제한다.")
    @Test
    public void deleteFacilitySchedule() {
        ScheduleReadDto targetSchedule = facilityScheduleService
            .find1DayFacilitySchedules(facility.getId(), nowDate).get(0);

        facilityScheduleService.deleteFacilitySchedule(targetSchedule.getId(),
            hostMember.getEmail());

        assertThatThrownBy(
            () -> facilityScheduleService.findFacilitySchedule(targetSchedule.getId()))
            .isInstanceOf(NotFoundEntityException.class);
    }

    @DisplayName("하루 치 스케줄데이터를 조회한다.")
    @Test
    public void find1DayFacilitySchedules() {
        List<ScheduleReadDto> facilitySchedules = facilityScheduleService
            .find1DayFacilitySchedules(facility.getId(), nowDate);

        assertThat(facilitySchedules).hasSize(1);
    }

    @DisplayName("1달 치 기본 스케줄을 생성한다.")
    @Test
    public void create1MonthDefaultFacilitySchedules_plus3Month() {
        YearMonth createYearMonth = nowYearMonth.plusMonths(3);

        facilityScheduleService
            .create1MonthDefaultFacilitySchedules(facility.getId(), createYearMonth,
                hostMember.getEmail());

        List<ScheduleReadDto> retReadDtos = facilityScheduleService
            .find1MonthFacilitySchedules(facility.getId(), createYearMonth);
        assertAll(
            () -> assertThat(retReadDtos.size()).isEqualTo(createYearMonth.lengthOfMonth()),
            () -> assertThat(retReadDtos.get(0).getDate())
                .isEqualTo(createYearMonth.atDay(1)),
            () -> assertThat(retReadDtos.get(retReadDtos.size() - 1).getDate())
                .isEqualTo(createYearMonth.atEndOfMonth())
        );
    }

    @DisplayName("1달 치 스케줄데이터를 조회한다.")
    @Test
    public void find1MonthFacilitySchedules() {
        List<ScheduleReadDto> facilitySchedules = facilityScheduleService
            .find1MonthFacilitySchedules(facility.getId(), nowYearMonth);

        assertThat(facilitySchedules).hasSize(nowYearMonth.lengthOfMonth());
    }

    @DisplayName("이미 생성되어있는 1달 치 스케줄을 지우고 새로 생성한다.")
    @Test
    public void create1MonthDefaultFacilitySchedules_plus2Month() {
        YearMonth createYearMonth = nowYearMonth.plusMonths(2);

        facilityScheduleService
            .create1MonthDefaultFacilitySchedules(facility.getId(), createYearMonth,
                hostMember.getEmail());

        List<ScheduleReadDto> retReadDtos = facilityScheduleService
            .find1MonthFacilitySchedules(facility.getId(), createYearMonth);
        assertAll(
            () -> assertThat(retReadDtos.size()).isEqualTo(createYearMonth.lengthOfMonth()),
            () -> assertThat(retReadDtos.get(0).getDate())
                .isEqualTo(createYearMonth.atDay(1)),
            () -> assertThat(
                retReadDtos.get(retReadDtos.size() - 1).getDate())
                .isEqualTo(createYearMonth.atEndOfMonth())
        );
    }

    @DisplayName("1달 치 스케줄을 삭제한다.")
    @Test
    public void delete1MonthFacilitySchedules() {
        YearMonth deleteYearMonth = nowYearMonth.plusMonths(2);

        facilityScheduleService
            .delete1MonthFacilitySchedules(facility.getId(), deleteYearMonth,
                hostMember.getEmail());

        List<ScheduleReadDto> retReadDtos = facilityScheduleService
            .find1MonthFacilitySchedules(facility.getId(), deleteYearMonth);
        assertThat(retReadDtos).isEmpty();
    }
}
