package com.modoospace.space.sevice;

import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.space.controller.dto.SpaceCreateUpdateDto;
import com.modoospace.space.controller.dto.SpaceReadDetailDto;
import com.modoospace.space.controller.dto.SpaceReadDto;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SpaceService {

  private final MemberRepository memberRepository;
  private final SpaceRepository spaceRepository;
  private final CategoryRepository categoryRepository;

  @Transactional
  public Long createSpace(Long categoryId, SpaceCreateUpdateDto createDto, String loginEmail) {
    Member host = findMemberByEmail(loginEmail);
    Category category = findCategoryById(categoryId);

    Space space = createDto.toEntity(category, host);
    spaceRepository.save(space);

    return space.getId();
  }

  public List<SpaceReadDto> findSpaceByCategory(Long categoryId) {
    // TODO : 페이징처리가 필요합니다.
    Category category = findCategoryById(categoryId);
    List<Space> spaces = spaceRepository.findByCategory(category);

    return SpaceReadDto.toDtos(spaces);
  }

  public List<SpaceReadDto> findSpaceByHost(Long hostId) {
    Member host = memberRepository.findById(hostId)
        .orElseThrow(() -> new NotFoundEntityException("사용자", hostId));
    List<Space> spaces = spaceRepository.findByHost(host);

    return SpaceReadDto.toDtos(spaces);
  }

  public SpaceReadDetailDto findSpace(Long spaceId) {
    Space space = findSpaceById(spaceId);

    return SpaceReadDetailDto.toDto(space);
  }

  @Transactional
  public void updateSpace(Long spaceId, SpaceCreateUpdateDto updateDto, String email) {
    Member loginMember = findMemberByEmail(email);
    Space space = findSpaceById(spaceId);

    space.update(updateDto.toEntity(space.getCategory(), space.getHost()), loginMember);
  }

  @Transactional
  public void deleteSpace(Long spaceId, String email) {
    Member loginMember = findMemberByEmail(email);
    Space space = findSpaceById(spaceId);

    // TODO: 예약이 존재하는지 확인이 필요합니다.

    space.verifyManagementPermission(loginMember);
    spaceRepository.delete(space);
  }

  private Member findMemberByEmail(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEntityException("사용자", email));
    return member;
  }

  public Space findSpaceById(Long spaceId) {
    Space space = spaceRepository.findById(spaceId)
        .orElseThrow(() -> new NotFoundEntityException("공간", spaceId));
    return space;
  }

  private Category findCategoryById(Long categoryId) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new NotFoundEntityException("카테고리", categoryId));
    return category;
  }
}
