package com.modoospace.space.sevice;

import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateRequest;
import com.modoospace.space.controller.dto.space.SpaceDetailResponse;
import com.modoospace.space.controller.dto.space.SpaceResponse;
import com.modoospace.space.controller.dto.space.SpaceSearchRequest;
import com.modoospace.space.domain.*;
import com.modoospace.space.repository.SpaceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final CategoryRepository categoryRepository;
    private final SpaceQueryRepository spaceQueryRepository;
    private final SpaceIndexRepository spaceIndexRepository;

    @Transactional
    @CacheEvict(value = "findSpaceId")
    public Long createSpace(Long categoryId, SpaceCreateUpdateRequest createRequest,
                            Member loginMember) {
        Category category = findCategoryById(categoryId);

        Space space = createRequest.toEntity(category, loginMember);
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
    @CacheEvict(value = "findSpaceId")
    public void updateSpace(Long spaceId, SpaceCreateUpdateRequest updateRequest,
                            Member loginMember) {
        Space space = findSpaceById(spaceId);
        space.verifyManagementPermission(loginMember);

        Space updatedSpace = updateRequest.toEntity(space.getCategory(), space.getHost());
        space.update(updatedSpace);
        spaceIndexRepository.save(SpaceIndex.of(space));
    }

    @Transactional
    @CacheEvict(value = "findSpaceId")
    public void deleteSpace(Long spaceId, Member loginMember) {
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
