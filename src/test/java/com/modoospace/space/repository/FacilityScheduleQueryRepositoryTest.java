package com.modoospace.space.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.modoospace.TestConfig;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.facility.FacilityCreateDto;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
public class FacilityScheduleQueryRepositoryTest {

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private SpaceRepository spaceRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityScheduleRepository facilityScheduleRepository;

  @Autowired
  private FacilityScheduleQueryRepository facilityScheduleQueryRepository;

  private Facility roomFacility;

  private LocalDate now;

  @BeforeEach
  public void setUp() {
    Member hostMember = Member.builder()
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
    Space space = spaceCreateDto.toEntity(category, hostMember);
    spaceRepository.save(space);

    FacilityCreateDto createRoomDto = FacilityCreateDto.builder()
        .name("스터디룸")
        .facilityType(FacilityType.ROOM)
        .description("1~4인실 입니다.")
        .reservationEnable(true)
        .build();
    roomFacility = facilityRepository.save(createRoomDto.toEntity(space));

    now = LocalDate.now();
  }

  @DisplayName("해당 기간에 시설이 Open했는지 여부를 반환한다.")
  @Test
  public void isIncludingSchedule() {
    LocalDateTime startDateTime = now.atTime(9, 0, 0);
    LocalDateTime endDateTime = now.atTime(11, 59, 59);
    assertThat(facilityScheduleQueryRepository
        .isIncludingSchedule(roomFacility, startDateTime, endDateTime)).isTrue();

    startDateTime = now.atTime(0, 0, 0);
    endDateTime = now.plusDays(6).atTime(23, 59, 59);
    assertThat(facilityScheduleQueryRepository
        .isIncludingSchedule(roomFacility, startDateTime, endDateTime)).isTrue();
  }

  @DisplayName("해당 기간에 시설이 Open했는지 여부를 반환한다. (날짜마다 시간범위가 다른 case)")
  @Test
  public void isIncludingSchedule_ifScheduleUpdate() {
    FacilitySchedule updateFacilitySchedule = FacilitySchedule.builder()
        .startDateTime(now.plusDays(1).atTime(0, 0, 0))
        .endDateTime(now.plusDays(1).atTime(17, 59, 59))
        .build(); // 오늘날짜 + 1 스케줄 데이터를 업데이트한다. (0~18로 변경, 다음날은 0~24)
    FacilitySchedule facilitySchedule = roomFacility.getFacilitySchedules().getFacilitySchedules()
        .get(now.getDayOfMonth());
    facilitySchedule.update(updateFacilitySchedule);

    LocalDateTime startDateTime = now.atTime(9, 0, 0);
    LocalDateTime endDateTime = now.plusDays(1).atTime(17, 59, 59);
    assertThat(facilityScheduleQueryRepository
        .isIncludingSchedule(roomFacility, startDateTime, endDateTime)).isTrue();

    endDateTime = now.plusDays(2).atTime(17, 59, 59);
    assertThat(facilityScheduleQueryRepository
        .isIncludingSchedule(roomFacility, startDateTime, endDateTime)).isFalse();
  }

  @DisplayName("해당 기간에 시설이 Open했는지 여부를 반환한다. (한 날짜에 스케줄이 여러개 있는 case)")
  @Test
  public void isIncludingSchedule_ifScheduleUpdate2() {
    FacilitySchedule facilitySchedule1 = FacilitySchedule.builder()
        .startDateTime(now.plusDays(1).atTime(9, 0, 0))
        .endDateTime(now.plusDays(1).atTime(11, 59, 59))
        .build();
    FacilitySchedule facilitySchedule2 = FacilitySchedule.builder()
        .startDateTime(now.plusDays(1).atTime(13, 0, 0))
        .endDateTime(now.plusDays(1).atTime(17, 59, 59))
        .build();// 오늘날짜 + 1 스케줄 데이터를 업데이트한다. (9~12, 13~18로 변경)

    List<FacilitySchedule> facilitySchedules = roomFacility.getFacilitySchedules()
        .getFacilitySchedules();
    facilitySchedules.remove(now.getDayOfMonth());
    facilitySchedules.add(facilitySchedule1);
    facilitySchedules.add(facilitySchedule2);
    facilityScheduleRepository.flush();

    LocalDateTime startDateTime = now.plusDays(1).atTime(9, 0, 0);
    LocalDateTime endDateTime = now.plusDays(1).atTime(17, 59, 59);
    assertThat(facilityScheduleQueryRepository
        .isIncludingSchedule(roomFacility, startDateTime, endDateTime)).isFalse();
  }
}
