package com.modoospace.space.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.TestConfig;
import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
import com.modoospace.space.controller.dto.space.SpaceReadDetailDto;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
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
  private SpaceCreateUpdateDto createDto;

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

    createDto = SpaceCreateUpdateDto.builder()
        .name("공간이름")
        .description("설명")
        .address(address)
        .build();
  }

  @DisplayName("로그인한 멤버가 호스트일 경우 공간을 등록할 수 있다.")
  @Test
  public void createSpace_IfHost() {
    Long spaceId = spaceService.createSpace(category.getId(), createDto, hostMember.getEmail());

    SpaceReadDetailDto retSpaceDto = spaceService.findSpace(spaceId);
    assertAll(
        () -> assertThat(retSpaceDto.getId()).isEqualTo(spaceId),
        () -> assertThat(retSpaceDto.getName()).isEqualTo("공간이름"),
        () -> assertThat(retSpaceDto.getDescription()).isEqualTo("설명"),
        () -> assertThat(retSpaceDto.getAddress()).isEqualTo(address)
    );
  }

  @DisplayName("로그인한 멤버가 호스트가 아닐 경우 공간 등록 시 예외를 던진다.")
  @ParameterizedTest
  @ValueSource(strings = {"admin@email", "visitor@email"})
  public void createSpace_throwException_IfNotHost(String email) {
    assertThatThrownBy(() -> spaceService.createSpace(category.getId(), createDto, email))
        .isInstanceOf(PermissionDeniedException.class);
  }

  @DisplayName("공간의 주인/관리자만이 공간을 수정할 수 있다.")
  @ParameterizedTest
  @ValueSource(strings = {"host@email", "admin@email"})
  public void updateSpace(String email) {
    Long spaceId = spaceService.createSpace(category.getId(), createDto, hostMember.getEmail());
    Address updateAddress = Address.builder()
        .depthFirst("시도")
        .depthSecond("구")
        .depthThird("동")
        .detailAddress("상세주소")
        .build();
    SpaceCreateUpdateDto updateDto = SpaceCreateUpdateDto.builder()
        .name("업데이트공간")
        .description("업데이트설명")
        .address(updateAddress)
        .build();

    spaceService.updateSpace(spaceId, updateDto, email);

    SpaceReadDetailDto retSpaceDto = spaceService.findSpace(spaceId);
    assertAll(
        () -> assertThat(retSpaceDto.getName()).isEqualTo("업데이트공간"),
        () -> assertThat(retSpaceDto.getDescription()).isEqualTo("업데이트설명"),
        () -> assertThat(retSpaceDto.getAddress()).isEqualTo(updateAddress)
    );
  }

  @DisplayName("공간의 주인/관리자가 아닐 경우 공간을 수정 시 예외를 던진다.")
  @Test
  public void updateSpace_throwException_IfAdminMemberOrOwnSpace() {
    Long spaceId = spaceService.createSpace(category.getId(), createDto, hostMember.getEmail());
    SpaceCreateUpdateDto updateDto = SpaceCreateUpdateDto.builder()
        .name("업데이트")
        .address(address)
        .build();

    assertAll(
        () -> assertThatThrownBy(
            () -> spaceService.updateSpace(spaceId, updateDto, "notMember@Email"))
            .isInstanceOf(NotFoundEntityException.class),
        () -> assertThatThrownBy(
            () -> spaceService.updateSpace(spaceId, updateDto, visitorMember.getEmail()))
            .isInstanceOf(PermissionDeniedException.class)
    );
  }

  @DisplayName("공간의 주인/관리자만이 공간을 삭제할 수 있다.")
  @ParameterizedTest
  @ValueSource(strings = {"host@email", "admin@email"})
  public void deleteSpace(String email) {
    Long spaceId = spaceService.createSpace(category.getId(), createDto, hostMember.getEmail());

    spaceService.deleteSpace(spaceId, email);

    assertThat(spaceRepository.existsById(spaceId)).isFalse();
  }

  @DisplayName("공간의 주인 또는 관리자가 아닐 경우 공간을 삭제 시 예외를 던진다.")
  @Test
  public void deleteSpace_throwException_IfAdminMemberOrOwnSpace() {
    Long spaceId = spaceService.createSpace(category.getId(), createDto, hostMember.getEmail());

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
    spaceService.createSpace(category.getId(), createDto, hostMember.getEmail());
    createDto = SpaceCreateUpdateDto.builder()
        .name("공간이름2")
        .address(address)
        .build();
    spaceService.createSpace(category.getId(), createDto, hostMember.getEmail());

    assertThat(spaceService.findSpaceByHost(hostMember.getId())).hasSize(2);
  }
}
