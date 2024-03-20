package com.modoospace.data.service;

import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.data.controller.dto.AddressResponse;
import com.modoospace.data.controller.dto.SpaceResponse;
import com.modoospace.member.domain.Member;
import com.modoospace.member.service.MemberService;
import com.modoospace.space.controller.dto.space.SpaceDetailResponse;
import com.modoospace.space.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataService {

    private final MemberService memberService;
    private final CategoryRepository categoryRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceIndexRepository spaceIndexRepository;
    private final FacilityRepository facilityRepository;

    public SpaceDetailResponse saveEntity(SpaceResponse spaceResponse, AddressResponse addressResponse, String email) {
        Space space = makeSpace(spaceResponse, addressResponse, email);
        spaceIndexRepository.save(SpaceIndex.of(space));

        List<Facility> facilities = makeFacility(spaceResponse, space);
        space.setFacilities(facilities);
        return SpaceDetailResponse.of(space);
    }

    public SpaceIndex saveIndex(Long spaceId){
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new NotFoundEntityException("공간", spaceId));
        return spaceIndexRepository.save(SpaceIndex.of(space));
    }

    private Space makeSpace(SpaceResponse spaceResponse, AddressResponse addressResponse, String email) {
        Member member = memberService.findMemberByEmail(email);
        Address address = addressResponse.toAddress(spaceResponse.getLocation().getDetailAddress());
        Category category = findCategory(spaceResponse.getCategoryName());

        return spaceRepository.save(spaceResponse.toSpace(address, category, member));
    }

    private Category findCategory(String name) {
        Optional<Category> optionalCategory = categoryRepository.findByName(name);
        return optionalCategory.orElseGet(() -> categoryRepository.save(new Category(name)));
    }

    private List<Facility> makeFacility(SpaceResponse spaceResponse, Space space) {
        return spaceResponse.getFacilityResponses().stream()
                .map(product -> facilityRepository.save(product.toFacility(spaceResponse.getTimeSettings(), spaceResponse.getWeekdaySettings(), space)))
                .collect(Collectors.toList());
    }
}
