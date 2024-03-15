package com.modoospace.data.controller.dto.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class JibunAddress {

    @JsonProperty("region_1depth_name")
    String depthFirst;

    @JsonProperty("region_2depth_name")
    String depthSecond;

    @JsonProperty("region_3depth_name")
    String depthThird;

    @JsonProperty("main_address_no")
    String addressNo;

    @JsonProperty("sub_address_no")
    String subAddressNo;

    public String getFullAddressNo() {
        return subAddressNo != null ? addressNo + "-" + subAddressNo : addressNo;
    }
}
