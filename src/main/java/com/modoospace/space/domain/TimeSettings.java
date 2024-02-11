package com.modoospace.space.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeSettings {

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TimeSetting> timeSettings = new ArrayList<>();

    public TimeSettings(List<TimeSetting> timeSettings) {
        this.timeSettings = validateAndMerge(timeSettings);
    }

    private List<TimeSetting> validateAndMerge(List<TimeSetting> timeSettings) {
        timeSettings.sort(Comparator.comparing(TimeSetting::getStartHour));

        while (validateAndMergeContinuousTime(timeSettings)) {
        }

        return timeSettings;
    }

    private boolean validateAndMergeContinuousTime(List<TimeSetting> timeSettings) {
        for (int i = 0; i < timeSettings.size() - 1; i++) {
            TimeSetting timeSetting1 = timeSettings.get(i);
            TimeSetting timeSetting2 = timeSettings.get(i + 1);

            timeSetting1.verifyConflicting(timeSetting2);
            if (timeSetting1.isContinuous(timeSetting2)) {
                timeSetting1.merge(timeSetting2);
                timeSettings.remove(timeSetting2);
                return true;
            }
        }
        return false;
    }

    public void setFacility(Facility facility) {
        for (TimeSetting timeSetting : timeSettings) {
            timeSetting.setFacility(facility);
        }
    }

    public List<Schedule> createSchedules(LocalDate date) {
        return timeSettings.stream()
            .map(timeSetting -> timeSetting.createSchedule(date))
            .collect(Collectors.toList());
    }

    public void update(TimeSettings timeSettings, Facility facility) {
        this.timeSettings.clear();
        timeSettings.setFacility(facility);
        this.timeSettings.addAll(timeSettings.getTimeSettings());
    }
}
