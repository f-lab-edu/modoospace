package com.modoospace.space.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modoospace.exception.HostPermissionException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.SpaceCreateDto;
import com.modoospace.space.controller.dto.SpaceReadDto;
import com.modoospace.space.domain.Address;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

    memberRepository.save(hostMember);
    memberRepository.save(visitorMember);
  }

  @DisplayName("공간을 등록한다.")
  @Test
  public void createSpace() {
    Address address = Address.builder()
        .fullAddress("fullAddress")
        .depthFirst("depthFirst")
        .depthSecond("depthSecond")
        .depthThird("depthThird")
        .build();
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .build();

    Long spaceId = spaceService.createSpace(createDto, hostMember.getEmail());

    SpaceReadDto retSpaceDto = spaceService.findSpaceById(spaceId);
    assertThat(retSpaceDto.getId()).isEqualTo(spaceId);
  }

  @DisplayName("Host가 아닐 경우 공간을 등록 시 예외를 던진다.")
  @Test
  public void createSpace_throwException_IfVisitorMember() {
    Address address = Address.builder()
        .fullAddress("fullAddress")
        .depthFirst("depthFirst")
        .depthSecond("depthSecond")
        .depthThird("depthThird")
        .build();
    SpaceCreateDto createDto = SpaceCreateDto.builder()
        .name("공간이름")
        .address(address)
        .build();

    assertThatThrownBy(() -> spaceService.createSpace(createDto, visitorMember.getEmail()))
        .isInstanceOf(HostPermissionException.class);
  }
}
