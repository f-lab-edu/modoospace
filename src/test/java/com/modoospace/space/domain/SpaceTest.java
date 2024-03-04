package com.modoospace.space.domain;

import com.modoospace.common.exception.DeleteSpaceWithFacilitiesException;
import com.modoospace.common.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class SpaceTest {

    private Member adminMember;
    private Member hostMember;
    private Member visitorMember;

    private Category category;

    private Address address;

    @BeforeEach
    public void setUp() {

        adminMember = Member.builder()
                .id(1L)
                .email("admin@email")
                .name("admin")
                .role(Role.ADMIN)
                .build();

        hostMember = Member.builder()
                .id(2L)
                .email("host@email")
                .name("host")
                .role(Role.HOST)
                .build();

        visitorMember = Member.builder()
                .id(3L)
                .email("visitor@email")
                .name("visitor")
                .role(Role.VISITOR)
                .build();

        category = new Category("스터디 공간");

        address = Address.builder()
                .depthFirst("depthFirst")
                .depthSecond("depthSecond")
                .depthThird("depthThird")
                .detailAddress("detailAddress")
                .build();
    }

    @DisplayName("호스트만이 공간을 가질 수 있다.")
    @Test
    public void space() {
        Space space = Space.builder()
                .name("공간이름")
                .description("테스트공간입니다.")
                .address(address)
                .category(category)
                .host(hostMember)
                .build();

        assertThat(space.getHost()).isEqualTo(hostMember);
    }

    @DisplayName("호스트가 아닐 경우 공간을 가질 수 없다.")
    @Test
    public void space_throwException_IfNotHost() {
        assertAll(
                () -> assertThatThrownBy(() -> Space.builder()
                        .name("공간이름")
                        .description("테스트공간입니다.")
                        .address(address)
                        .category(category)
                        .host(visitorMember)
                        .build()),
                () -> assertThatThrownBy(() -> Space.builder()
                        .name("공간이름")
                        .description("테스트공간입니다.")
                        .address(address)
                        .category(category)
                        .host(adminMember)
                        .build())
        );
    }

    @DisplayName("공간의 주인/관리자만이 공간 수정/삭제를 할 수 있음을 검증한다.")
    @Test
    public void verifyManagementPermission() {
        Space space = Space.builder()
                .name("공간이름")
                .description("테스트공간입니다.")
                .address(address)
                .category(category)
                .host(hostMember)
                .build();

        assertAll(
                () -> space.verifyManagementPermission(hostMember),
                () -> space.verifyManagementPermission(adminMember)
        );
    }

    @DisplayName("공간의 주인/관리자가 아닐 경우 공간 수정/삭제 권한 검증 시 예외를 던진다.")
    @Test
    public void verifyManagementPermission_throwException_ifNotPermission() {
        Space space = Space.builder()
                .name("공간이름")
                .description("테스트공간입니다.")
                .address(address)
                .category(category)
                .host(hostMember)
                .build();

        assertThatThrownBy(() -> space.verifyManagementPermission(visitorMember))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @DisplayName("삭제 권한 확인 시 해당 공간이 시설을 갖고있다면 예외를 던진다.")
    @Test
    public void verifyDeletePermission_throwException_ifSpaceHasFacility() {
        Facility facility = Facility.builder()
                .name("4인실")
                .minUser(2)
                .maxUser(4)
                .reservationEnable(true)
                .build();
        Space space = Space.builder()
                .name("공간이름")
                .description("테스트공간입니다.")
                .address(address)
                .category(category)
                .host(hostMember)
                .facilities(Collections.singletonList(facility))
                .build();

        assertThatThrownBy(() -> space.verifyDeletePermission(hostMember))
                .isInstanceOf(DeleteSpaceWithFacilitiesException.class);
    }
}
