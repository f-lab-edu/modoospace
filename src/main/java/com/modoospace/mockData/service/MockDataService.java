package com.modoospace.mockData.service;

import com.modoospace.member.domain.Member;
import com.modoospace.mockData.controller.dto.MockAddressResponse;
import com.modoospace.mockData.controller.dto.MockSpaceResponse;
import com.modoospace.space.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MockDataService {

    private final CategoryRepository categoryRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceIndexRepository spaceIndexRepository;
    private final FacilityRepository facilityRepository;

    public Long saveEntity(MockSpaceResponse spaceResponse, MockAddressResponse addressResponse, Member loginMember) {
        Space space = makeSpace(spaceResponse, addressResponse, loginMember);
        spaceIndexRepository.save(SpaceIndex.of(space));
        makeFacility(spaceResponse, space);
        return space.getId();
    }

    private Space makeSpace(MockSpaceResponse spaceResponse, MockAddressResponse addressResponse, Member loginMember) {
        Address address = addressResponse.toAddress(spaceResponse.getLocation().getDetailAddress());
        Category category = findCategory(spaceResponse.getCategoryName());

        return spaceRepository.save(spaceResponse.toSpace(address, category, loginMember));
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
