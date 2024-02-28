package com.modoospace.space.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.modoospace.JpaTestConfig;
import com.modoospace.SpaceIndexTestRepository;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.reservation.domain.DateTimeRange;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.space.controller.dto.facility.FacilityCreateRequest;
import com.modoospace.space.controller.dto.space.SpaceSearchRequest;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateRequest;
import com.modoospace.space.domain.Address;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceIndex;
import com.modoospace.space.domain.SpaceRepository;
import com.modoospace.space.domain.TimeRange;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
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
@Import(JpaTestConfig.class)
@ActiveProfiles("test")
public class SpaceQueryRepositoryTest {

    @Autowired
    private SpaceQueryRepository spaceQueryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    private SpaceIndexTestRepository spaceIndexTestRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Member visitorMember;

    private Facility facility;

    private LocalDate now;

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        spaceIndexTestRepository = new SpaceIndexTestRepository(restHighLevelClient);

        Member hostMember = Member.builder()
            .email("host@email")
            .name("host")
            .role(Role.HOST)
            .build();
        memberRepository.save(hostMember);

        visitorMember = Member.builder()
            .email("visitor@email")
            .name("visitor")
            .role(Role.VISITOR)
            .build();
        memberRepository.save(visitorMember);

        Category category = new Category("스터디 공간");
        categoryRepository.save(category);

        Address address = Address.builder()
            .depthFirst("서울")
            .depthSecond("관악구")
            .depthThird("남현동")
            .build();
        Space space = Space.builder()
            .name("사당 스터디룸")
            .description("사당역 3번출구 5분거리에요.")
            .category(category)
            .host(hostMember)
            .address(address)
            .build();
        spaceRepository.save(space);
        spaceIndexTestRepository.save(SpaceIndex.of(space));

        Address address2 = Address.builder()
            .depthFirst("서울")
            .depthSecond("서초구")
            .depthThird("서초동")
            .build();
        Space space2 = Space.builder()
            .name("강남 스터디룸")
            .description("강남역 3번출구 5분거리에요.")
            .category(category)
            .host(hostMember)
            .address(address2)
            .build();
        spaceRepository.save(space2);
        spaceIndexTestRepository.save(SpaceIndex.of(space2));

        // TimeSetting, WeekSetting 기본값이 필요하여 Request 사용.
        FacilityCreateRequest createRequest = FacilityCreateRequest.builder()
            .name("A스터디룸")
            .reservationEnable(true)
            .minUser(1)
            .maxUser(4)
            .description("1~4인실 입니다.")
            .build();
        facility = facilityRepository.save(createRequest.toEntity(space));

        FacilityCreateRequest createRequest2 = FacilityCreateRequest.builder()
            .name("B스터디룸")
            .reservationEnable(true)
            .minUser(3)
            .maxUser(8)
            .timeSettings(List.of(new TimeSettingCreateRequest(8, 23)))
            .description("3~8인실 입니다.")
            .build();
        facilityRepository.save(createRequest2.toEntity(space2));

        now = LocalDate.now();
    }

    @DisplayName("쿼리(사당)에 맞는 공간을 반환한다.")
    @Test
    public void searchSpace_byQuery_사당() {
        SpaceSearchRequest searchRequest = new SpaceSearchRequest();
        searchRequest.setQuery("사당");

        Page<Space> resultPage = spaceQueryRepository
            .searchSpace(searchRequest, PageRequest.of(0, 10));

        assertThat(resultPage.getContent()).extracting("name")
            .containsExactly("사당 스터디룸");
    }

    @DisplayName("쿼리(스터디룸)에 맞는 공간을 반환한다.")
    @Test
    public void searchSpace_byQuery_스터디룸() {
        SpaceSearchRequest searchRequest = new SpaceSearchRequest();
        searchRequest.setQuery("스터디룸");

        Page<Space> resultPage = spaceQueryRepository
            .searchSpace(searchRequest, PageRequest.of(0, 10));

        assertThat(resultPage.getContent()).extracting("name")
            .containsExactly("사당 스터디룸", "강남 스터디룸");
    }

    @DisplayName("지역에 맞는 공간을 반환한다.")
    @Test
    public void searchSpace_byAddress() {
        SpaceSearchRequest searchRequest = new SpaceSearchRequest();
        searchRequest.setDepthFirst("서울");
        searchRequest.setDepthSecond("관악구");

        Page<Space> resultPage = spaceQueryRepository
            .searchSpace(searchRequest, PageRequest.of(0, 10));

        assertThat(resultPage.getContent()).extracting("name")
            .containsExactly("사당 스터디룸");
    }

    @DisplayName("최대 인원에 맞는 공간을 반환한다.")
    @Test
    public void searchSpace_byMinMaxUser() {
        SpaceSearchRequest searchRequest = new SpaceSearchRequest();
        searchRequest.setMaxUser(5);

        Page<Space> resultPage = spaceQueryRepository
            .searchSpace(searchRequest, PageRequest.of(0, 10));

        assertThat(resultPage.getContent()).extracting("name")
            .containsExactly("강남 스터디룸");
    }

    @DisplayName("사용일자와 시간에 맞는 공간을 반환한다.")
    @Test
    public void searchSpace_byUseDateTime() {
        SpaceSearchRequest searchRequest = new SpaceSearchRequest();
        searchRequest.setUseDate(now);
        searchRequest.setTimeRange(new TimeRange(5, 8));

        Page<Space> resultPage = spaceQueryRepository
            .searchSpace(searchRequest, PageRequest.of(0, 10));

        assertThat(resultPage.getContent()).extracting("name")
            .containsExactly("사당 스터디룸");
    }

    @DisplayName("사용일자와 시간에 맞는 공간을 반환한다.(예약이 존재하는 케이스)")
    @Test
    public void searchSpace_byUseDateTime_ifReservationExist() {
        DateTimeRange dateTimeRange = new DateTimeRange(now, 9, now, 12);
        Reservation reservation = Reservation.builder()
            .numOfUser(3)
            .dateTimeRange(dateTimeRange)
            .visitor(visitorMember)
            .facility(facility)
            .build();
        reservationRepository.save(reservation);

        SpaceSearchRequest searchRequest = new SpaceSearchRequest();
        searchRequest.setUseDate(now);
        searchRequest.setTimeRange(new TimeRange(9, 11));

        Page<Space> resultPage = spaceQueryRepository
            .searchSpace(searchRequest, PageRequest.of(0, 10));

        assertThat(resultPage.getContent()).extracting("name")
            .containsExactly("강남 스터디룸");
    }

    @AfterEach
    public void clear() throws IOException {
        spaceIndexTestRepository.deleteAll();
    }
}
