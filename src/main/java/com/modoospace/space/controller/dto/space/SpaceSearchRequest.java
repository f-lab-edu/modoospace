package com.modoospace.space.controller.dto.space;

import com.modoospace.common.DateFormatManager;
import com.modoospace.space.domain.TimeRange;
import java.time.LocalDate;
import javax.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
public class SpaceSearchRequest {

    private String query;

    private String depthFirst; // 시도

    private String depthSecond; // 구

    private String depthThird; // 동

    @Positive
    private Integer maxUser; // 최대인원

    @DateTimeFormat(pattern = DateFormatManager.DATE_FORMAT)
    private LocalDate useDate; // 사용일자

    private Integer startHour; // 시작시간

    private Integer endHour; // 시작시간

    private TimeRange timeRange; // 사용시간

    public void updateTimeRange() {
        this.timeRange = startHour != null && endHour != null ?
            new TimeRange(startHour, endHour) : null;
    }
}
