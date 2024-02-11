package com.modoospace.space.domain;

import com.modoospace.common.exception.ConflictingTimeException;
import com.sun.istack.NotNull;
import java.time.LocalDate;
import java.time.YearMonth;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule {

    @Id
    @GeneratedValue
    @Column(name = "schedule_id")
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @NotNull
    @Embedded
    private TimeRange timeRange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    public Schedule(LocalDate date, TimeRange timeRange) {
        this(null, date, timeRange, null);
    }

    @Builder
    public Schedule(Long id, LocalDate date, TimeRange timeRange, Facility facility) {
        this.id = id;
        this.date = date;
        this.timeRange = timeRange;
        this.facility = facility;
    }

    public void verifyConflicting(Schedule targetSchedule) {
        if (isConflicting(targetSchedule)) {
            throw new ConflictingTimeException(this, targetSchedule);
        }
    }

    private boolean isConflicting(Schedule targetSchedule) {
        if (!isDateEqual(targetSchedule)) {
            return false;
        }
        return timeRange.isConflicting(targetSchedule.getTimeRange());
    }

    public boolean isContinuous(Schedule targetSchedule) {
        if (!isDateEqual(targetSchedule)) {
            return false;
        }
        return timeRange.isContinuous(targetSchedule.getTimeRange());
    }

    public void merge(Schedule targetSchedule) {
        timeRange.mergeTimeRange(targetSchedule.getTimeRange());
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public boolean isYearMonthEqual(YearMonth yearMonth) {
        return YearMonth.of(date.getYear(), date.getMonth()).equals(yearMonth);
    }

    private boolean isDateEqual(Schedule targetSchedule) {
        return this.date.isEqual(targetSchedule.getDate());
    }

    public void update(Schedule schedule) {
        this.date = schedule.getDate();
        this.timeRange.update(schedule.getTimeRange());
    }

    public Boolean isBetween(Integer hour) {
        return hour < getEndHour() && hour >= getStartHour();
    }

    public Integer getStartHour() {
        return this.timeRange.getStartHour();
    }

    public Integer getEndHour() {
        return this.timeRange.getEndHour();
    }

    @Override
    public String toString() {
        return "Schedule{" + date.toString() + "=" + timeRange.getStartTime().toString() + "~"
            + timeRange.getEndTime().toString() + "}";
    }
}
