package com.modoospace.space.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.SpaceCreateDto;
import com.modoospace.space.controller.dto.SpaceReadDto;
import com.modoospace.space.controller.dto.SpaceUpdateDto;
import com.modoospace.space.domain.Address;
import com.modoospace.space.domain.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class SpaceServiceTest {

  private SpaceService spaceService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private SpaceRepository spaceRepository;

  private Member hostMember;
  private Member visitorMember;
  private Member adminMember;
  private Address address;

  @BeforeEach
  public void setUp() {
    spaceService = new SpaceService(memberRepository, spaceRepository);

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

    address = Address.builder()
        .depthFirst("depthFirst")
        .depthSecond("depthSecond")
        .depthThird("depthThird")
        .detailAddress("detailAddress")
        .build();
  }

  @DisplayName("호스트일 경우 공간을 등록할 수 있다.")
  @Test
  public void createSpace() {
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .build();

    Long spaceId = spaceService.createSpace(createDto, hostMember.getEmail());

    SpaceReadDto retSpaceDto = spaceService.findSpace(spaceId);
    assertThat(retSpaceDto.getId()).isEqualTo(spaceId);
  }

  @DisplayName("Host가 아닐 경우 공간을 등록 시 예외를 던진다.")
  @Test
  public void createSpace_throwException_IfVisitorMember() {
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .build();

    assertAll(
        () -> assertThatThrownBy(() -> spaceService.createSpace(createDto, "notMember@Email"))
            .isInstanceOf(NotFoundEntityException.class),
        () -> assertThatThrownBy(
            () -> spaceService.createSpace(createDto, visitorMember.getEmail()))
            .isInstanceOf(PermissionDeniedException.class)
    );
  }

  @DisplayName("본인의 공간 OR 관리자일 경우 공간을 업데이트할 수 있다.")
  @ParameterizedTest
  @ValueSource(strings = {"host@email", "admin@email"})
  public void updateSpace(String testEmail) {
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .build();
    Long spaceId = spaceService.createSpace(createDto, hostMember.getEmail());
    Address updateAddress = Address.builder()
        .depthFirst("시도")
        .depthSecond("구")
        .depthThird("동")
        .detailAddress("상세주소")
        .build();
    SpaceUpdateDto updateDto = SpaceUpdateDto.builder()
        .id(spaceId)
        .name("업데이트공간")
        .address(updateAddress)
        .build();

    spaceService.updateSpace(updateDto, testEmail);

    SpaceReadDto retSpaceDto = spaceService.findSpace(spaceId);
    assertThat(retSpaceDto.getName()).isEqualTo("업데이트공간");
    assertThat(retSpaceDto.getAddress()).isEqualTo(updateAddress);
  }

  @DisplayName("본인의 공간 AND 관리자가 아닐 경우 공간을 수정 시 예외를 던진다.")
  @Test
  public void updateSpace_throwException_IfAdminMemberOrOwnSpace() {
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .build();
    Long spaceId = spaceService.createSpace(createDto, hostMember.getEmail());
    SpaceUpdateDto updateDto = SpaceUpdateDto.builder()
        .id(spaceId)
        .name("업데이트")
        .address(address)
        .build();

    assertAll(
        () -> assertThatThrownBy(() -> spaceService.updateSpace(updateDto, "notMember@Email"))
            .isInstanceOf(NotFoundEntityException.class),
        () -> assertThatThrownBy(
            () -> spaceService.updateSpace(updateDto, visitorMember.getEmail()))
            .isInstanceOf(PermissionDeniedException.class)
    );
  }

  @DisplayName("본인의 공간 OR 관리자일 경우 공간을 삭제할 수 있다.")
  @ParameterizedTest
  @ValueSource(strings = {"host@email", "admin@email"})
  public void deleteSpace(String testEmail) {
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .build();
    Long spaceId = spaceService.createSpace(createDto, hostMember.getEmail());

    spaceService.deleteSpace(spaceId, testEmail);

    assertThat(spaceRepository.existsById(spaceId)).isFalse();
  }

  @DisplayName("본인의 공간 AND 관리자가 아닐 경우 공간을 삭제 시 예외를 던진다.")
  @Test
  public void deleteSpace_throwException_IfAdminMemberOrOwnSpace() {
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .build();
    Long spaceId = spaceService.createSpace(createDto, hostMember.getEmail());

    assertAll(
        () -> assertThatThrownBy(() -> spaceService.deleteSpace(spaceId, "notMember@Email"))
            .isInstanceOf(NotFoundEntityException.class),
        () -> assertThatThrownBy(
            () -> spaceService.deleteSpace(spaceId, visitorMember.getEmail()))
            .isInstanceOf(PermissionDeniedException.class)
    );
  }

  @DisplayName("호스트ID로 호스트가 소유하고있는 공간을 조회할 수 있다.")
  @Test
  public void findSpaceByHost() {
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .build();
    spaceService.createSpace(createDto, hostMember.getEmail());
    createDto = SpaceCreateDto.builder()
        .name("공간이름2")
        .address(address)
        .build();
    spaceService.createSpace(createDto, hostMember.getEmail());

    assertThat(spaceService.findSpaceByHost(hostMember.getId())).hasSize(2);
  }
}
