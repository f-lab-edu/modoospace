package com.modoospace.mockData.service;

import com.modoospace.member.domain.Member;
import com.modoospace.member.service.MemberService;
import com.modoospace.mockData.controller.dto.MockAddressResponse;
import com.modoospace.mockData.controller.dto.MockSpaceResponse;
import com.modoospace.space.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MockDataService {

    private final MemberService memberService;
    private final CategoryRepository categoryRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceIndexRepository spaceIndexRepository;
    private final FacilityRepository facilityRepository;

    public Long saveEntity(MockSpaceResponse spaceResponse, MockAddressResponse addressResponse, String email) {
        Space space = makeSpace(spaceResponse, addressResponse, email);
        spaceIndexRepository.save(SpaceIndex.of(space));
        makeFacility(spaceResponse, space);
        return space.getId();
    }

    private Space makeSpace(MockSpaceResponse spaceResponse, MockAddressResponse addressResponse, String email) {
        Member member = memberService.findMemberByEmail(email);
        Address address = addressResponse.toAddress(spaceResponse.getLocation().getDetailAddress());
        Category category = findCategory(spaceResponse.getCategoryName());

        return spaceRepository.save(spaceResponse.toSpace(address, category, member));
    }

    private Category findCategory(String name) {
        Optional<Category> optionalCategory = categoryRepository.findByName(name);
        return optionalCategory.orElseGet(() -> categoryRepository.save(new Category(name)));
    }

    private void makeFacility(MockSpaceResponse spaceResponse, Space space) {
        spaceResponse.getFacilityResponses()
                .forEach(product -> facilityRepository.save(product.toFacility(spaceResponse.getTimeSettings(), spaceResponse.getWeekdaySettings(), space)));
    }
}
