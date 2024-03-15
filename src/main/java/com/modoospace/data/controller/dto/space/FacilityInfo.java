package com.modoospace.data.controller.dto.space;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacilityInfo {

    String name;

    @JsonProperty("desc")
    String description;

    @JsonProperty("min_guest_policy")
    Integer minUser = 1;

    @JsonProperty("max_guest_capacity")
    Integer maxUser = 10;
}
