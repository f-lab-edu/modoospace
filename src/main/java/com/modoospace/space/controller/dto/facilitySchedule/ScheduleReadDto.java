package com.modoospace.space.controller.dto.facilitySchedule;

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
public class ScheduleReadDto {

    @NotNull
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatManager.DATE_FORMAT)
    private LocalDate date;

    private Integer startHour;

    private Integer endHour;

    @Builder
    public ScheduleReadDto(Long id, LocalDate date, Integer startHour, Integer endHour) {
        this.id = id;
        this.date = date;
        this.startHour = startHour;
        this.endHour = endHour;
    }


    public static ScheduleReadDto toDto(Schedule schedule) {
        return ScheduleReadDto.builder()
            .id(schedule.getId())
            .date(schedule.getDate())
            .startHour(schedule.getStartHour())
            .endHour(schedule.getEndHour())
            .build();
    }

    public static List<ScheduleReadDto> toDtos(List<Schedule> schedules) {
        return schedules.stream()
            .map(ScheduleReadDto::toDto)
            .collect(Collectors.toList());
    }
}
