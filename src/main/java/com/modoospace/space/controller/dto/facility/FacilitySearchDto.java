package com.modoospace.space.controller.dto.facility;

import com.modoospace.space.domain.FacilityType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * query string 형식으로 dto를 받기 위해서는 setter 필요
 */
@Getter @Setter
@NoArgsConstructor
public class FacilitySearchDto {

  private String name;

  private FacilityType facilityType;

  private Boolean reservationEnable;

  public FacilitySearchDto(String name, FacilityType facilityType,
      Boolean reservationEnable) {
    this.name = name;
    this.facilityType = facilityType;
    this.reservationEnable = reservationEnable;
  }
}
