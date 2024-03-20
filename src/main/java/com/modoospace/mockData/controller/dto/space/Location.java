package com.modoospace.mockData.controller.dto.space;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {

    @JsonProperty("addr")
    String address;

    @JsonProperty("addr_detail")
    String detailAddress;
}
