package com.modoospace.space.sevice;

import com.modoospace.AbstractIntegrationContainerBaseTest;
import com.modoospace.common.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.address.AddressCreateUpdateRequest;
import com.modoospace.space.controller.dto.facility.FacilityCreateRequest;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateRequest;
import com.modoospace.space.controller.dto.space.SpaceDetailResponse;
import com.modoospace.space.controller.dto.space.SpaceResponse;
import com.modoospace.space.controller.dto.space.SpaceSearchRequest;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
class SpaceServiceTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Member hostMember;
    private Member visitorMember;

    private Member adminMember;

    private AddressCreateUpdateRequest createAddress;
    private Category category;

    @BeforeEach
    public void setUp() {
        hostMember = Member.builder()
                .email("host@email")
                .name("host")
                .role(Role.HOST)
                .build();

        visitorMember = Member.builder()
                .email("visitor@email")
                .name("visitor")
                .role(Role.VISITOR)
                .build();

        adminMember = Member.builder()
                .email("admin@email")
                .name("admin")
                .role(Role.ADMIN)
                .build();

        hostMember = memberRepository.save(hostMember);
        visitorMember = memberRepository.save(visitorMember);
        adminMember = memberRepository.save(adminMember);

        createAddress = AddressCreateUpdateRequest.builder()
                .depthFirst("depthFirst")
                .depthSecond("depthSecond")
                .depthThird("depthThird")
                .detailAddress("detailAddress")
                .build();

        category = new Category("스터디 공간");
        categoryRepository.save(category);
    }

    @DisplayName("로그인한 멤버가 호스트일 경우 공간을 등록할 수 있다.")
    @Test
    public void createSpace_IfHost() {
        SpaceCreateUpdateRequest createRequest = new SpaceCreateUpdateRequest(
                "공간이름", "설명", createAddress);
        Long spaceId = spaceService.createSpace(category.getId(), createRequest,
                hostMember);

        SpaceDetailResponse retSpaceResponse = spaceService.findSpace(spaceId);
        assertAll(
                () -> assertThat(retSpaceResponse.getId()).isEqualTo(spaceId),
                () -> assertThat(retSpaceResponse.getName()).isEqualTo("공간이름"),
                () -> assertThat(retSpaceResponse.getDescription()).isEqualTo("설명"),
                () -> assertThat(retSpaceResponse.getAddress().getDepthFirst())
                        .isEqualTo(createAddress.getDepthFirst()),
                () -> assertThat(retSpaceResponse.getAddress().getDepthSecond())
                        .isEqualTo(createAddress.getDepthSecond()),
                () -> assertThat(retSpaceResponse.getAddress().getDepthThird())
                        .isEqualTo(createAddress.getDepthThird())
        );
    }

    @DisplayName("로그인한 멤버가 호스트가 아닐 경우 공간 등록 시 예외를 던진다.")
    @Test
    public void createSpace_throwException_IfNotHost() {
        SpaceCreateUpdateRequest createRequest = new SpaceCreateUpdateRequest(
                "공간이름", "설명", createAddress);

        assertThatThrownBy(() -> spaceService.createSpace(category.getId(), createRequest, adminMember))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> spaceService.createSpace(category.getId(), createRequest, visitorMember))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @DisplayName("쿼리(공간)에 맞는 공간을 검색한다.(ElasticSearch 활용)")
    @Test
    public void searchSpace() {
        SpaceCreateUpdateRequest createRequest = new SpaceCreateUpdateRequest(
                "공간이름", "설명", createAddress);
        Long spaceId = spaceService.createSpace(category.getId(), createRequest, hostMember);
        FacilityCreateRequest facilityCreateRequest = FacilityCreateRequest.builder()
                .name("스터디룸1")
                .reservationEnable(true)
                .minUser(1)
                .maxUser(4)
                .description("1~4인실 입니다.")
                .build();
        facilityService.createFacility(spaceId, facilityCreateRequest, hostMember);

        SpaceSearchRequest spaceSearchRequest = new SpaceSearchRequest();
        spaceSearchRequest.setQuery("공간");
        Page<SpaceResponse> spaceResponses = spaceService.searchSpace(spaceSearchRequest, PageRequest.of(0, 10));

        assertThat(spaceResponses.getContent()).extracting("name")
                .containsExactly("공간이름");
    }

    @DisplayName("쿼리(공간)에 맞는 공간을 검색한다.(쿼리 활용)")
    @Test
    public void searchSpaceQuery() {
        SpaceCreateUpdateRequest createRequest = new SpaceCreateUpdateRequest(
                "공간이름", "설명", createAddress);
        Long spaceId = spaceService.createSpace(category.getId(), createRequest, hostMember);
        FacilityCreateRequest facilityCreateRequest = FacilityCreateRequest.builder()
                .name("스터디룸1")
                .reservationEnable(true)
                .minUser(1)
                .maxUser(4)
                .description("1~4인실 입니다.")
                .build();
        facilityService.createFacility(spaceId, facilityCreateRequest, hostMember);

        SpaceSearchRequest spaceSearchRequest = new SpaceSearchRequest();
        spaceSearchRequest.setQuery("공간");
        Page<SpaceResponse> spaceResponses = spaceService.searchSpaceQuery(spaceSearchRequest, PageRequest.of(0, 10));

        assertThat(spaceResponses.getContent()).extracting("name")
                .containsExactly("공간이름");
    }


    @DisplayName("공간의 주인은 공간을 수정할 수 있다.")
    @Test
    public void updateSpace_byHost() {
        updateSpace(hostMember);
    }

    @DisplayName("관리자는 공간을 수정할 수 있다.")
    @Test
    public void updateSpace_byAdmin() {
        updateSpace(adminMember);
    }

    private void updateSpace(Member loginMember) {
        SpaceCreateUpdateRequest createRequest = new SpaceCreateUpdateRequest(
                "공간이름", "설명", createAddress);
        Long spaceId = spaceService.createSpace(category.getId(), createRequest,
                hostMember);
        AddressCreateUpdateRequest updateAddress = AddressCreateUpdateRequest.builder()
                .depthFirst("시도")
                .depthSecond("구")
                .depthThird("동")
                .detailAddress("상세주소")
                .build();
        SpaceCreateUpdateRequest updateRequest = new SpaceCreateUpdateRequest("업데이트공간", "업데이트설명",
                updateAddress);

        spaceService.updateSpace(spaceId, updateRequest, loginMember);

        SpaceDetailResponse retSpaceResponse = spaceService.findSpace(spaceId);
        assertAll(
                () -> assertThat(retSpaceResponse.getName()).isEqualTo("업데이트공간"),
                () -> assertThat(retSpaceResponse.getDescription()).isEqualTo("업데이트설명"),
                () -> assertThat(retSpaceResponse.getAddress().getDepthFirst())
                        .isEqualTo(updateAddress.getDepthFirst()),
                () -> assertThat(retSpaceResponse.getAddress().getDepthSecond())
                        .isEqualTo(updateAddress.getDepthSecond()),
                () -> assertThat(retSpaceResponse.getAddress().getDepthThird())
                        .isEqualTo(updateAddress.getDepthThird())
        );
    }

    @DisplayName("공간의 주인/관리자가 아닐 경우 공간을 수정 시 예외를 던진다.")
    @Test
    public void updateSpace_throwException_IfAdminMemberOrOwnSpace() {
        SpaceCreateUpdateRequest createRequest = new SpaceCreateUpdateRequest(
                "공간이름", "설명", createAddress);
        Long spaceId = spaceService.createSpace(category.getId(), createRequest,
                hostMember);
        SpaceCreateUpdateRequest updateRequest = new SpaceCreateUpdateRequest(
                "업데이트공간", "업데이트설명", createAddress);


        assertThatThrownBy(
                () -> spaceService.updateSpace(spaceId, updateRequest, visitorMember))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @DisplayName("공간의 주인은 공간을 삭제할 수 있다.")
    @Test
    public void deleteSpace_byHost() {
        deleteSpace(hostMember);
    }

    @DisplayName("관리자는 공간을 삭제할 수 있다.")
    @Test
    public void deleteSpace_byAdmin() {
        deleteSpace(adminMember);
    }

    private void deleteSpace(Member loginMember) {
        SpaceCreateUpdateRequest createRequest = new SpaceCreateUpdateRequest(
                "공간이름", "설명", createAddress);
        Long spaceId = spaceService.createSpace(category.getId(), createRequest,
                hostMember);

        spaceService.deleteSpace(spaceId, loginMember);

        assertThat(spaceRepository.existsById(spaceId)).isFalse();
    }

    @DisplayName("공간의 주인 또는 관리자가 아닐 경우 공간을 삭제 시 예외를 던진다.")
    @Test
    public void deleteSpace_throwException_IfAdminMemberOrOwnSpace() {
        SpaceCreateUpdateRequest createRequest = new SpaceCreateUpdateRequest(
                "공간이름", "설명", createAddress);
        Long spaceId = spaceService.createSpace(category.getId(), createRequest,
                hostMember);

        assertThatThrownBy(
                () -> spaceService.deleteSpace(spaceId, visitorMember))
                .isInstanceOf(PermissionDeniedException.class);
    }
}
