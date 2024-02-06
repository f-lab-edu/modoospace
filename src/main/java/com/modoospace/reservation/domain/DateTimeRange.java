package com.modoospace.reservation.domain;

import com.modoospace.common.exception.InvalidTimeRangeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DateTimeRange {

    @NotNull
    private LocalDateTime startDateTime;

    @NotNull
    private LocalDateTime endDateTime;

    public DateTimeRange(LocalDate startDate, Integer startHour, LocalDate endDate,
        Integer endHour) {
        this(LocalDateTime.of(startDate, LocalTime.of(startHour, 0)),
            LocalDateTime.of(endDate, LocalTime.of(endHour - 1, 59)));
    }

    public DateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        validateDateTimes(startDateTime, endDateTime);
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    private void validateDateTimes(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (!startDateTime.isBefore(endDateTime)) {
            throw new InvalidTimeRangeException(startDateTime, endDateTime);
        }
    }

    public boolean isConflicting(DateTimeRange targetTimeRage) {
        // 12 ~ 14,  13 ~ 14 O
        // 12 ~ 13,  13 ~ 14 x
        return startDateTime.isBefore(targetTimeRage.getEndDateTime())
            && endDateTime.isAfter(targetTimeRage.getStartDateTime());
    }

    public void update(DateTimeRange dateTimeRange) {
        this.startDateTime = dateTimeRange.getStartDateTime();
        this.endDateTime = dateTimeRange.getEndDateTime();
    }

    public LocalDate getStartDate() {
        return this.startDateTime.toLocalDate();
    }

    public LocalTime getStartTime() {
        return this.startDateTime.toLocalTime();
    }

    public Integer getStartHour() {
        return this.startDateTime.getHour();
    }

    public LocalDate getEndDate() {
        return this.endDateTime.toLocalDate();
    }

    public LocalTime getEndTime() {
        return this.endDateTime.toLocalTime();
    }

    public Integer getEndHour() {
        return this.endDateTime.getHour() + 1;
    }
}
