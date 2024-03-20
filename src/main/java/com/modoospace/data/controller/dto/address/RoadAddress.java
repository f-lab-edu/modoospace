package com.modoospace.data.controller.dto.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoadAddress {

    @JsonProperty("road_name")
    String roadName;

    @JsonProperty("main_building_no")
    String buildingNo;

    @JsonProperty("sub_building_no")
    String subBuildingNo;

    public String getFullBuildingNo() {
        return subBuildingNo == null || subBuildingNo.isBlank() ? buildingNo : buildingNo + "-" + subBuildingNo;
    }
}
