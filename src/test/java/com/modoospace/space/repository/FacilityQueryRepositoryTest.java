package com.modoospace.space.repository;

import com.modoospace.JpaTestConfig;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.facility.FacilityCreateRequest;
import com.modoospace.space.controller.dto.facility.FacilitySearchRequest;
import com.modoospace.space.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaTestConfig.class)
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

        Category category = new Category("스터디 공간");
        categoryRepository.save(category);

        space = Space.builder()
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
        facilityRepository.save(createRequest.toEntity(space));

        FacilityCreateRequest createRequest2 = FacilityCreateRequest.builder()
                .name("스터디룸2")
                .reservationEnable(true)
                .minUser(1)
                .maxUser(8)
                .description("1~8인실 입니다.")
                .build();
        facilityRepository.save(createRequest2.toEntity(space));
    }

    @DisplayName("검색 조건에 맞는 시설을 반환한다.")
    @Test
    public void searchFacility() {
        FacilitySearchRequest searchRequest = new FacilitySearchRequest();
        searchRequest.setName("스터디룸1");
        searchRequest.setReservationEnable(true);

        Page<Facility> resultPage = facilityQueryRepository
                .searchFacility(space.getId(), searchRequest, PageRequest.of(0, 10));

        assertThat(resultPage.getContent()).extracting("name")
                .containsExactly("스터디룸1");
    }

    @DisplayName("검색 조건에 맞는 시설이 없다면 빈 페이지를 반환한다.")
    @Test
    public void searchFacility_emptyPage() {
        FacilitySearchRequest searchRequest = new FacilitySearchRequest();
        searchRequest.setReservationEnable(false);

        Page<Facility> resultPage = facilityQueryRepository
                .searchFacility(space.getId(), searchRequest, PageRequest.of(0, 10));

        assertThat(resultPage.getContent()).isEmpty();
    }

    @DisplayName("검색 조건이 없다면, 해당 Space의 모든 시설을 반환한다.")
    @Test
    public void searchFacility_AllFacility() {
        FacilitySearchRequest searchRequest = new FacilitySearchRequest();

        Page<Facility> resultPage = facilityQueryRepository
                .searchFacility(space.getId(), searchRequest, PageRequest.of(0, 10));

        assertThat(resultPage.getContent()).extracting("name")
                .containsExactly("스터디룸1", "스터디룸2");
    }
}
