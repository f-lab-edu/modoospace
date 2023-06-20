package com.modoospace.space.controller.dto;

import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityType;
import com.modoospace.space.domain.Setting;
import com.modoospace.space.domain.Space;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
public class FacilityCreateUpdateDto {

  @NotEmpty
  private String name;

  @NotNull
  private FacilityType facilityType;

  @NotNull
  private Boolean reservationEnable;

  private String desc;

  @Builder.Default
  private List<SettingCreateDto> settingCreateDtos = List.of(new SettingCreateDto());

  public FacilityCreateUpdateDto(String name, FacilityType facilityType, Boolean reservationEnable,
      String desc, List<SettingCreateDto> settingCreateDtos) {
    this.name = name;
    this.facilityType = facilityType;
    this.reservationEnable = reservationEnable;
    this.desc = desc;
    this.settingCreateDtos = settingCreateDtos;
  }

  public Facility toEntity(Space space) {
    return Facility.builder()
        .name(name)
        .facilityType(facilityType)
        .reservationEnable(reservationEnable)
        .desc(desc)
        .space(space)
        .settings(toSettings(settingCreateDtos))
        .build();
  }

  private List<Setting> toSettings(List<SettingCreateDto> settingCreateDtos) {
    // TODO : 시작시간 순서대로 정렬 후 겹치는 시간이 있는지 validation 필요
    return settingCreateDtos.stream()
        .map(settingCreateDto -> settingCreateDto.toEntity())
        .collect(Collectors.toList());
  }
}
