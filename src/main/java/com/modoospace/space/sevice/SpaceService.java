package com.modoospace.space.sevice;

import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.service.MemberService;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateRequest;
import com.modoospace.space.controller.dto.space.SpaceDetailResponse;
import com.modoospace.space.controller.dto.space.SpaceResponse;
import com.modoospace.space.controller.dto.space.SpaceSearchRequest;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceIndex;
import com.modoospace.space.domain.SpaceIndexRepository;
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
    private final SpaceIndexRepository spaceIndexRepository;

    @Transactional
    public Long createSpace(Long categoryId, SpaceCreateUpdateRequest createRequest,
        String loginEmail) {
        Member host = memberService.findMemberByEmail(loginEmail);
        Category category = findCategoryById(categoryId);

        Space space = createRequest.toEntity(category, host);
        spaceRepository.save(space);
        spaceIndexRepository.save(SpaceIndex.of(space));

        return space.getId();
    }

    public Page<SpaceResponse> searchSpace(SpaceSearchRequest searchRequest, Pageable pageable) {
        Page<Space> spaces = spaceQueryRepository.searchSpace(searchRequest, pageable);

        return spaces.map(SpaceResponse::of);
    }

    public Page<SpaceResponse> searchSpaceQuery(SpaceSearchRequest searchRequest, Pageable pageable) {
        Page<Space> spaces = spaceQueryRepository.searchSpaceQuery(searchRequest, pageable);

        return spaces.map(SpaceResponse::of);
    }

    public SpaceDetailResponse findSpace(Long spaceId) {
        Space space = findSpaceById(spaceId);

        return SpaceDetailResponse.of(space);
    }

    @Transactional
    public void updateSpace(Long spaceId, SpaceCreateUpdateRequest updateRequest,
        String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Space space = findSpaceById(spaceId);
        space.verifyManagementPermission(loginMember);

        Space updatedSpace = updateRequest.toEntity(space.getCategory(), space.getHost());
        space.update(updatedSpace);
        spaceIndexRepository.save(SpaceIndex.of(space));
    }

    @Transactional
    public void deleteSpace(Long spaceId, String loginEmail) {
        Member loginMember = memberService.findMemberByEmail(loginEmail);
        Space space = findSpaceById(spaceId);
        space.verifyDeletePermission(loginMember);

        spaceRepository.delete(space);
        spaceIndexRepository.delete(SpaceIndex.of(space));
    }

    private Space findSpaceById(Long spaceId) {
        return spaceRepository.findById(spaceId)
            .orElseThrow(() -> new NotFoundEntityException("공간", spaceId));
    }

    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new NotFoundEntityException("카테고리", categoryId));
    }
}
