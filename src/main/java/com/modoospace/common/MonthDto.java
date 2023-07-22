package com.modoospace.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.YearMonth;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MonthDto {

  @NotNull
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
  private YearMonth yearMonth;

  public MonthDto(YearMonth yearMonth) {
    this.yearMonth = yearMonth;
  }
}
