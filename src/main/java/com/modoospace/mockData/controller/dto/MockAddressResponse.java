package com.modoospace.mockData.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.modoospace.mockData.controller.dto.address.Document;
import com.modoospace.space.domain.Address;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MockAddressResponse {

    List<Document> documents = new ArrayList<>();

    public Address toAddress(String detailAddress) {
        Document document = documents.get(0);
        return Address.builder()
                .depthFirst(document.getAddress().getDepthFirst())
                .depthSecond(document.getAddress().getDepthSecond())
                .depthThird(document.getAddress().getDepthThird())
                .addressNo(document.getAddress().getFullAddressNo())
                .roadName(document.getRoadAddress().getRoadName())
                .buildingNo(document.getRoadAddress().getFullBuildingNo())
                .detailAddress(detailAddress)
                .x(document.getX())
                .y(document.getY())
                .build();
    }
}
