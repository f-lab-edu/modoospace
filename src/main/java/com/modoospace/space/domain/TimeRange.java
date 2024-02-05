package com.modoospace.space.domain;

import com.modoospace.common.exception.InvalidTimeRangeException;
import java.time.LocalTime;
import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeRange {

    private LocalTime startTime;

    private LocalTime endTime;

    public TimeRange(Integer start, Integer end) {
        this(LocalTime.of(start, 0), LocalTime.of(end - 1, 59));
    }

    public TimeRange(LocalTime startTime, LocalTime endTime) {
        validateHours(startTime, endTime);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private void validateHours(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new InvalidTimeRangeException(startTime, endTime);
        }
    }

    public boolean isConflicting(TimeRange targetRange) {
        // 12~14, 14~16 X
        // 12~24, 13~16 O
        return startTime.isBefore(targetRange.getEndTime())
            && endTime.isAfter(targetRange.getStartTime());
    }

    public void mergeTimeRange(TimeRange timeRange) {
        this.endTime = timeRange.getEndTime();
    }

    public boolean isContinuous(TimeRange targetRange) {
        return endTime.plusMinutes(1).equals(targetRange.getStartTime());
    }

    public void update(TimeRange timeRange) {
        this.startTime = timeRange.getStartTime();
        this.endTime = timeRange.getEndTime();
    }

    public Integer getStartHour() {
        return this.startTime.getHour();
    }

    public Integer getEndHour() {
        return this.endTime.getHour() + 1;
    }
}
