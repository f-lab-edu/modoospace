package com.modoospace.reservation.controller.dto;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AvailabilityTimeRequestDto {

  @NotNull
  private Long id;
  @NotNull
  private LocalDate requestDate;

  public AvailabilityTimeRequestDto(Long id, LocalDate requestDate){
    this.id =id;
    this.requestDate = requestDate;
  }
}
