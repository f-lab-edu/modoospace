package com.modoospace.space.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.FacilityCreateDto;
import com.modoospace.space.controller.dto.SpaceCreateUpdateDto;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.FacilityType;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
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

  private Member hostMember;
  private Space space;

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
  }

  @DisplayName("시설 생성 시 Setting시간을 선택하지않으면 24시간 예약이 가능한 시설이 생성된다.")
  @Test
  public void createFacility_24HourOpen_ifNotSelectSetting() {
    FacilityCreateDto createDto = FacilityCreateDto.builder()
        .name("스터디룸1")
        .facilityType(FacilityType.ROOM)
        .description("1~4인실 입니다.")
        .reservationEnable(false)
        .build();

    Long facilityId = facilityService
        .createFacility(space.getId(), createDto, hostMember.getEmail());
    Facility facility = facilityRepository.findById(facilityId).get();

    assertAll(
        () -> assertThat(facility.getId()).isEqualTo(facilityId),
        () -> assertThat(facility.getName()).isEqualTo("스터디룸1"),
        () -> assertThat(facility.getFacilityType()).isEqualTo(FacilityType.ROOM),
        () -> assertThat(facility.getDescription()).isEqualTo("1~4인실 입니다."),
        () -> assertThat(facility.getReservationEnable()).isFalse()
    );
  }
}
