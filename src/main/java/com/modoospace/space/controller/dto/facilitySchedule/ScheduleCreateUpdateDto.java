package com.modoospace.space.controller.dto.facilitySchedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modoospace.common.DateFormatManager;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.Schedule;
import com.modoospace.space.domain.TimeRange;
import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScheduleCreateUpdateDto {

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatManager.DATE_FORMAT)
    private LocalDate date;

    @NotNull
    private Integer startHour;

    @NotNull
    private Integer endHour;

    public ScheduleCreateUpdateDto(LocalDate date, Integer startHour, Integer endHour) {
        this.date = date;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public Schedule toEntity(Facility facility) {
        return Schedule.builder()
            .date(date)
            .timeRange(new TimeRange(startHour, endHour))
            .facility(facility)
            .build();
    }
}
