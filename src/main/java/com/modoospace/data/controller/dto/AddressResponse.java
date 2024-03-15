package com.modoospace.data.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.modoospace.data.controller.dto.address.Document;
import com.modoospace.space.domain.Address;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressResponse {

    List<Document> documents;

    public Address toAddress() {
        Document document = documents.get(0);
        return Address.builder()
                .depthFirst(document.getAddress().getDepthFirst())
                .depthSecond(document.getAddress().getDepthSecond())
                .depthThird(document.getAddress().getDepthThird())
                .addressNo(document.getAddress().getFullAddressNo())
                .roadName(document.getRoadAddress().getRoadName())
                .buildingNo(document.getRoadAddress().getFullBuildingNo())
                .x(document.getX())
                .y(document.getY())
                .build();
    }
}
