package com.modoospace.space.sevice;

import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.SpaceCreateDto;
import com.modoospace.space.controller.dto.SpaceReadDto;
import com.modoospace.space.controller.dto.SpaceUpdateDto;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SpaceService {

  private final MemberRepository memberRepository;
  private final SpaceRepository spaceRepository;

  @Transactional
  public Long createSpace(SpaceCreateDto createDto, String email) {
    Member member = findMemberByEmail(email);
    Space space = createDto.toEntity(member);

    spaceRepository.save(space);

    return space.getId();
  }

  public SpaceReadDto findSpace(Long spaceId) {
    Space space = findSpaceById(spaceId);

    return SpaceReadDto.toDto(space);
  }

  @Transactional
  public void updateSpace(SpaceUpdateDto updateDto, String email) {
    Member loginMember = findMemberByEmail(email);
    Space space = findSpaceById(updateDto.getId());

    checkPermission(space.getHost(), loginMember);

    space.update(updateDto.getName(), updateDto.getAddress());
  }

  @Transactional
  public void deleteSpace(Long spaceId, String email) {
    Member loginMember = findMemberByEmail(email);
    Space space = findSpaceById(spaceId);

    checkPermission(space.getHost(), loginMember);

    // TODO: 예약이 존재하는지 확인이 필요합니다.

    spaceRepository.delete(space);
  }

  private Member findMemberByEmail(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEntityException("사용자"));
    return member;
  }

  private Space findSpaceById(Long spaceId) {
    Space space = spaceRepository.findById(spaceId)
        .orElseThrow(() -> new NotFoundEntityException("공간"));
    return space;
  }

  private void checkPermission(Member host, Member loginMember) {
    if (!host.isEqual(loginMember) && !loginMember.isRoleEqual(Role.ADMIN)) {
      throw new PermissionDeniedException();
    }
  }
}
