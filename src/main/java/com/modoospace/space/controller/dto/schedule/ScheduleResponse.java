package com.modoospace.space.controller.dto.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modoospace.common.DateFormatManager;
import com.modoospace.space.domain.Schedule;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class ScheduleResponse {

    @NotNull
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatManager.DATE_FORMAT)
    private LocalDate date;

    private Integer startHour;

    private Integer endHour;

    @Builder
    public ScheduleResponse(Long id, LocalDate date, Integer startHour, Integer endHour) {
        this.id = id;
        this.date = date;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public static ScheduleResponse of(Schedule schedule) {
        return ScheduleResponse.builder()
            .id(schedule.getId())
            .date(schedule.getDate())
            .startHour(schedule.getStartHour())
            .endHour(schedule.getEndHour())
            .build();
    }

    public static List<ScheduleResponse> of(List<Schedule> schedules) {
        return schedules.stream()
            .map(ScheduleResponse::of)
            .collect(Collectors.toList());
    }
}
