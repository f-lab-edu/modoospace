package com.modoospace.space.domain;

import com.modoospace.common.exception.ConflictingTimeException;
import com.sun.istack.NotNull;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeSetting {

    @Id
    @GeneratedValue
    @Column(name = "time_setting_id")
    private Long id;

    @NotNull
    @Embedded
    private TimeRange timeRange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    public TimeSetting(TimeRange timeRange) {
        this(null, timeRange, null);
    }

    public TimeSetting(Long id, TimeRange timeRange, Facility facility) {
        this.id = id;
        this.timeRange = timeRange;
        this.facility = facility;
    }

    public void verifyConflicting(TimeSetting targetTimeSetting) {
        if (isConflicting(targetTimeSetting)) {
            throw new ConflictingTimeException(this, targetTimeSetting);
        }
    }

    private boolean isConflicting(TimeSetting targetTimeSetting) {
        return this.timeRange.isConflicting(targetTimeSetting.getTimeRange());
    }

    public boolean isContinuous(TimeSetting targetTimeSetting) {
        return this.timeRange.isContinuous(targetTimeSetting.getTimeRange());
    }

    public void merge(TimeSetting targetTimeSetting) {
        this.timeRange.mergeTimeRange(targetTimeSetting.getTimeRange());
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public Schedule createSchedule(LocalDate date) {
        return Schedule.builder().date(date).timeRange(this.timeRange).facility(this.facility)
            .build();
    }

    public Integer getStartHour() {
        return timeRange.getStartHour();
    }

    public Integer getEndHour() {
        return timeRange.getEndHour();
    }
}
