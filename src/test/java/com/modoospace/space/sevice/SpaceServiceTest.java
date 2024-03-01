package com.modoospace.space.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.common.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.address.AddressCreateUpdateRequest;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateRequest;
import com.modoospace.space.controller.dto.space.SpaceDetailResponse;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SpaceServiceTest {

    @Autowired
    private SpaceService spaceService;

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

        memberRepository.save(hostMember);
        memberRepository.save(visitorMember);
        memberRepository.save(adminMember);

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
            hostMember.getEmail());

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
    @ParameterizedTest
    @ValueSource(strings = {"admin@email", "visitor@email"})
    public void createSpace_throwException_IfNotHost(String email) {
        SpaceCreateUpdateRequest createRequest = new SpaceCreateUpdateRequest(
            "공간이름", "설명", createAddress);
        assertThatThrownBy(() -> spaceService.createSpace(category.getId(), createRequest, email))
            .isInstanceOf(PermissionDeniedException.class);
    }

    @DisplayName("공간의 주인/관리자만이 공간을 수정할 수 있다.")
    @ParameterizedTest
    @ValueSource(strings = {"host@email", "admin@email"})
    public void updateSpace(String email) {
        SpaceCreateUpdateRequest createRequest = new SpaceCreateUpdateRequest(
            "공간이름", "설명", createAddress);
        Long spaceId = spaceService.createSpace(category.getId(), createRequest,
            hostMember.getEmail());
        AddressCreateUpdateRequest updateAddress = AddressCreateUpdateRequest.builder()
            .depthFirst("시도")
            .depthSecond("구")
            .depthThird("동")
            .detailAddress("상세주소")
            .build();
        SpaceCreateUpdateRequest updateRequest = new SpaceCreateUpdateRequest("업데이트공간", "업데이트설명",
            updateAddress);

        spaceService.updateSpace(spaceId, updateRequest, email);

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
            hostMember.getEmail());
        SpaceCreateUpdateRequest updateRequest = new SpaceCreateUpdateRequest(
            "업데이트공간", "업데이트설명", createAddress);

        assertAll(
            () -> assertThatThrownBy(
                () -> spaceService.updateSpace(spaceId, updateRequest, "notMember@Email"))
                .isInstanceOf(NotFoundEntityException.class),
            () -> assertThatThrownBy(
                () -> spaceService.updateSpace(spaceId, updateRequest, visitorMember.getEmail()))
                .isInstanceOf(PermissionDeniedException.class)
        );
    }

    @DisplayName("공간의 주인/관리자만이 공간을 삭제할 수 있다.")
    @ParameterizedTest
    @ValueSource(strings = {"host@email", "admin@email"})
    public void deleteSpace(String email) {
        SpaceCreateUpdateRequest createRequest = new SpaceCreateUpdateRequest(
            "공간이름", "설명", createAddress);
        Long spaceId = spaceService.createSpace(category.getId(), createRequest,
            hostMember.getEmail());

        spaceService.deleteSpace(spaceId, email);

        assertThat(spaceRepository.existsById(spaceId)).isFalse();
    }

    @DisplayName("공간의 주인 또는 관리자가 아닐 경우 공간을 삭제 시 예외를 던진다.")
    @Test
    public void deleteSpace_throwException_IfAdminMemberOrOwnSpace() {
        SpaceCreateUpdateRequest createRequest = new SpaceCreateUpdateRequest(
            "공간이름", "설명", createAddress);
        Long spaceId = spaceService.createSpace(category.getId(), createRequest,
            hostMember.getEmail());

        assertAll(
            () -> assertThatThrownBy(() -> spaceService.deleteSpace(spaceId, "notMember@Email"))
                .isInstanceOf(NotFoundEntityException.class),
            () -> assertThatThrownBy(
                () -> spaceService.deleteSpace(spaceId, visitorMember.getEmail()))
                .isInstanceOf(PermissionDeniedException.class)
        );
    }
}
