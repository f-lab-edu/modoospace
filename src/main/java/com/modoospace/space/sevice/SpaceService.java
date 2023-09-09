package com.modoospace.space.sevice;

import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.service.MemberService;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
import com.modoospace.space.controller.dto.space.SpaceReadDto;
import com.modoospace.space.controller.dto.space.SpaceSearchDto;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import com.modoospace.space.repository.SpaceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SpaceService {

  private final MemberService memberService;
  private final SpaceRepository spaceRepository;
  private final CategoryRepository categoryRepository;
  private final SpaceQueryRepository spaceQueryRepository;

  @Transactional
  public Long createSpace(Long categoryId, SpaceCreateUpdateDto createDto, String loginEmail) {
    Member host = memberService.findMemberByEmail(loginEmail);
    Category category = findCategoryById(categoryId);

    Space space = createDto.toEntity(category, host);
    spaceRepository.save(space);

    return space.getId();
  }

  public Page<SpaceReadDto> searchSpace(SpaceSearchDto searchDto, Pageable pageable) {
    Page<Space> spaces = spaceQueryRepository.searchSpace(searchDto, pageable);

    return spaces.map(SpaceReadDto::toDto);
  }

  public SpaceReadDto findSpace(Long spaceId) {
    Space space = findSpaceById(spaceId);

    return SpaceReadDto.toDto(space);
  }

  @Transactional
  public void updateSpace(Long spaceId, SpaceCreateUpdateDto updateDto, String loginEmail) {
    Member loginMember = memberService.findMemberByEmail(loginEmail);
    Space space = findSpaceById(spaceId);
    Space updatedSpace = updateDto.toEntity(space.getCategory(), space.getHost());

    space.update(updatedSpace, loginMember);
  }

  @Transactional
  public void deleteSpace(Long spaceId, String loginEmail) {
    Member loginMember = memberService.findMemberByEmail(loginEmail);
    Space space = findSpaceById(spaceId);

    // TODO: 예약이 존재하는지 확인이 필요합니다.

    space.verifyManagementPermission(loginMember);
    spaceRepository.delete(space);
  }

  private Space findSpaceById(Long spaceId) {
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
