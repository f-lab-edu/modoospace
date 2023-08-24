package com.modoospace.space.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.modoospace.TestConfig;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.facility.FacilityCreateDto;
import com.modoospace.space.controller.dto.facility.FacilityReadDto;
import com.modoospace.space.controller.dto.facility.FacilitySearchDto;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.FacilityType;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
public class FacilityQueryRepositoryTest {

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private SpaceRepository spaceRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityQueryRepository facilityQueryRepository;

  private Space space;

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
    space = spaceCreateDto.toEntity(category, hostMember);
    spaceRepository.save(space);

    FacilityCreateDto createRoomDto = FacilityCreateDto.builder()
        .name("스터디룸")
        .facilityType(FacilityType.ROOM)
        .description("1~4인실 입니다.")
        .reservationEnable(true)
        .build();
    facilityRepository.save(createRoomDto.toEntity(space));
    FacilityCreateDto createSeatDto = FacilityCreateDto.builder()
        .name("스터디좌석")
        .facilityType(FacilityType.SEAT)
        .description("스터디좌석 입니다.")
        .reservationEnable(true)
        .build();
    facilityRepository.save(createSeatDto.toEntity(space));
  }

  @DisplayName("검색 조건에 맞는 시설을 반환한다.")
  @Test
  public void searchFacility() {
    FacilitySearchDto searchDto = new FacilitySearchDto();
    searchDto.setName("스터디");
    searchDto.setReservationEnable(true);

    Page<FacilityReadDto> resultPage = facilityQueryRepository
        .searchFacility(space.getId(), searchDto, PageRequest.of(0, 10));

    assertThat(resultPage.getContent()).extracting("name")
        .containsExactly("스터디룸", "스터디좌석");
  }

  @DisplayName("검색 조건에 맞는 시설이 없다면 빈 페이지를 반환한다.")
  @Test
  public void searchFacility_emptyPage() {
    FacilitySearchDto searchDto = new FacilitySearchDto();
    searchDto.setReservationEnable(false);

    Page<FacilityReadDto> resultPage = facilityQueryRepository
        .searchFacility(space.getId(), searchDto, PageRequest.of(0, 10));

    assertThat(resultPage.getContent()).isEmpty();
  }
}
