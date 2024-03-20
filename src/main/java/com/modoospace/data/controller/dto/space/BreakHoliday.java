package com.modoospace.data.controller.dto.space;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BreakHoliday {

    @JsonProperty("BRK_DAY_TP_CD")
    String breakDayCd;

    @JsonProperty("DAYW_CD")
    String dayCd;
}
