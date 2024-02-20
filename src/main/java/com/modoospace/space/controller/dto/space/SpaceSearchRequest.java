package com.modoospace.space.controller.dto.space;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modoospace.common.DateFormatManager;
import com.modoospace.space.domain.TimeRange;
import java.time.LocalDate;
import javax.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpaceSearchRequest {

    private String query;

    @Positive
    private Integer maxUser; // 최대인원

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatManager.DATE_FORMAT)
    private LocalDate useDate; // 사용일자

    private TimeRange timeRange; // 사용시간

    @Builder
    public SpaceSearchRequest(String query, Integer maxUser, LocalDate useDate, Integer startHour,
        Integer endHour) {
        this.query = query;
        this.maxUser = maxUser;
        this.useDate = useDate;
        if(startHour != null && endHour != null){
            this.timeRange = new TimeRange(startHour, endHour);
        }
    }
}
