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
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
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

  @Autowired
  private CategoryRepository categoryRepository;

  private Member hostMember;
  private Member visitorMember;
  private Member adminMember;
  private Address address;
  private Category category;

  @BeforeEach
  public void setUp() {
    spaceService = new SpaceService(memberRepository, spaceRepository, categoryRepository);

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

    category = Category.builder()
        .name("스터디 공간")
        .build();

    categoryRepository.save(category);
  }

  @DisplayName("로그인한 멤버가 호스트일 경우 본인의 공간을 등록할 수 있다.")
  @Test
  public void createOwnSpace_IfHost() {
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .categoryId(category.getId())
        .hostEmail(hostMember.getEmail())
        .build();

    Long spaceId = spaceService.createSpace(createDto, hostMember.getEmail());

    SpaceReadDto retSpaceDto = spaceService.findSpace(spaceId);
    assertThat(retSpaceDto.getId()).isEqualTo(spaceId);
  }

  @DisplayName("로그인한 멤버가 호스트가 아닐 경우 본인 공간 등록 시 예외를 던진다.")
  @ParameterizedTest
  @ValueSource(strings = {"admin@email", "visitor@email"})
  public void createOwnSpace_throwException_IfNotHost(String email) {
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .categoryId(category.getId())
        .hostEmail(email)
        .build();

    assertThatThrownBy(() -> spaceService.createSpace(createDto, email))
        .isInstanceOf(PermissionDeniedException.class);
  }

  @DisplayName("로그인한 멤버가 관리자일 경우 다른 호스트의 공간을 등록할 수 있다.")
  @Test
  public void createOtherSpace_IfAdmin() {
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .categoryId(category.getId())
        .hostEmail(hostMember.getEmail())
        .build();

    Long spaceId = spaceService.createSpace(createDto, adminMember.getEmail());

    SpaceReadDto retSpaceDto = spaceService.findSpace(spaceId);
    assertThat(retSpaceDto.getId()).isEqualTo(spaceId);
  }

  @DisplayName("로그인한 멤버가 호스트일지라도 본인 공간을 생성하는게 아니라면 예외를 던진다.")
  @Test
  public void createOtherSpace_throwException_IfNotAdmin() {
    Member hostMember2 = Member.builder()
        .email("host2@email")
        .name("host2")
        .role(Role.HOST)
        .build();
    memberRepository.save(hostMember2);
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .categoryId(category.getId())
        .hostEmail(hostMember2.getEmail())
        .build();

    assertThatThrownBy(() -> spaceService.createSpace(createDto, hostMember.getEmail()))
        .isInstanceOf(PermissionDeniedException.class);
  }

  @DisplayName("공간의 주인/관리자만이 공간을 수정할 수 있다.")
  @ParameterizedTest
  @ValueSource(strings = {"host@email", "admin@email"})
  public void updateSpace(String email) {
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .categoryId(category.getId())
        .hostEmail(hostMember.getEmail())
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
        .categoryId(category.getId())
        .build();

    spaceService.updateSpace(updateDto, email);

    SpaceReadDto retSpaceDto = spaceService.findSpace(spaceId);
    assertThat(retSpaceDto.getName()).isEqualTo("업데이트공간");
    assertThat(retSpaceDto.getAddress()).isEqualTo(updateAddress);
  }

  @DisplayName("공간의 주인/관리자가 아닐 경우 공간을 수정 시 예외를 던진다.")
  @Test
  public void updateSpace_throwException_IfAdminMemberOrOwnSpace() {
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .categoryId(category.getId())
        .hostEmail(hostMember.getEmail())
        .build();
    Long spaceId = spaceService.createSpace(createDto, hostMember.getEmail());
    SpaceUpdateDto updateDto = SpaceUpdateDto.builder()
        .id(spaceId)
        .name("업데이트")
        .address(address)
        .categoryId(category.getId())
        .build();

    assertAll(
        () -> assertThatThrownBy(() -> spaceService.updateSpace(updateDto, "notMember@Email"))
            .isInstanceOf(NotFoundEntityException.class),
        () -> assertThatThrownBy(
            () -> spaceService.updateSpace(updateDto, visitorMember.getEmail()))
            .isInstanceOf(PermissionDeniedException.class)
    );
  }

  @DisplayName("공간의 주인/관리자만이 공간을 삭제할 수 있다.")
  @ParameterizedTest
  @ValueSource(strings = {"host@email", "admin@email"})
  public void deleteSpace(String testEmail) {
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .categoryId(category.getId())
        .hostEmail(hostMember.getEmail())
        .build();
    Long spaceId = spaceService.createSpace(createDto, hostMember.getEmail());

    spaceService.deleteSpace(spaceId, testEmail);

    assertThat(spaceRepository.existsById(spaceId)).isFalse();
  }

  @DisplayName("공간의 주인 또는 관리자가 아닐 경우 공간을 삭제 시 예외를 던진다.")
  @Test
  public void deleteSpace_throwException_IfAdminMemberOrOwnSpace() {
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .categoryId(category.getId())
        .hostEmail(hostMember.getEmail())
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
        .categoryId(category.getId())
        .hostEmail(hostMember.getEmail())
        .build();
    spaceService.createSpace(createDto, hostMember.getEmail());
    createDto = SpaceCreateDto.builder()
        .name("공간이름2")
        .address(address)
        .categoryId(category.getId())
        .hostEmail(hostMember.getEmail())
        .build();
    spaceService.createSpace(createDto, hostMember.getEmail());

    assertThat(spaceService.findSpaceByHost(hostMember.getId())).hasSize(2);
  }
}
