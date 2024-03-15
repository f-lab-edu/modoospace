package com.modoospace.data.controller.dto.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {

    JibunAddress address;

    @JsonProperty("road_address")
    RoadAddress roadAddress;

    String x;
    String y;
}
