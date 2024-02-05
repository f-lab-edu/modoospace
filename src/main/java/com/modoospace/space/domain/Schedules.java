package com.modoospace.space.domain;

import com.modoospace.common.exception.ConflictingTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
public class Schedules {

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Schedule> schedules = new ArrayList<>();

    public Schedules(List<Schedule> schedules) {
        this.schedules = validateAndMerge(schedules);
    }

    private List<Schedule> validateAndMerge(List<Schedule> schedules) {
        Collections.sort(schedules, Comparator
            .comparing(Schedule::getDate)
            .thenComparing(Schedule::getStartHour));

        while (validateAndMergeContinuousSchedule(schedules)) {
        }

        return schedules;
    }

    private boolean validateAndMergeContinuousSchedule(List<Schedule> schedules) {
        for (int i = 0; i < schedules.size() - 1; i++) {
            Schedule schedule1 = schedules.get(i);
            Schedule schedule2 = schedules.get(i + 1);

            schedule1.verifyConflicting(schedule2);
            if (schedule1.isContinuous(schedule2)) {
                schedule1.merge(schedule2);
                schedules.remove(schedule2);
                return true;
            }
        }
        return false;
    }

    public static Schedules create3MonthFacilitySchedules(TimeSettings timeSettings,
        WeekdaySettings weekdaySettings, YearMonth createYearMonth) {
        LocalDate startDate = createYearMonth.atDay(1);
        int daysBetween = startDate.lengthOfMonth()
            + startDate.plusMonths(1).lengthOfMonth()
            + startDate.plusMonths(2).lengthOfMonth();

        return createFacilitySchedules(timeSettings, weekdaySettings, startDate, daysBetween);
    }

    public static Schedules create1MonthFacilitySchedules(TimeSettings timeSettings,
        WeekdaySettings weekdaySettings, YearMonth createYearMonth) {
        LocalDate startDate = createYearMonth.atDay(1);
        int daysBetween = startDate.lengthOfMonth();

        return createFacilitySchedules(timeSettings, weekdaySettings, startDate, daysBetween);
    }

    private static Schedules createFacilitySchedules(TimeSettings timeSettings,
        WeekdaySettings weekdaySettings, LocalDate startDate, int daysBetween) {
        List<Schedule> schedules = new ArrayList<>();
        IntStream.range(0, daysBetween)
            .mapToObj(startDate::plusDays)
            .filter(scheduleDate -> weekdaySettings.isContainWeekday(scheduleDate.getDayOfWeek()))
            .flatMap(scheduleDate -> timeSettings.createSchedules(scheduleDate).stream())
            .forEach(schedules::add);

        return new Schedules(schedules);
    }

    public void addSchedule(Schedule createSchedule) {
        schedules.add(createSchedule);
        validateAndMerge(schedules);
    }

    public void updateSchedule(Schedule updateSchedule, Schedule schedule) {
        schedule.update(updateSchedule);
        validateAndMerge(schedules);
    }

    public void update(Schedules schedules, Facility facility) {
        this.schedules.clear();
        this.schedules.addAll(schedules.getSchedules());
        schedules.setFacility(facility);
    }

    public void addAll(Schedules schedules, Facility facility) {
        this.schedules.addAll(schedules.getSchedules());
        schedules.setFacility(facility);
    }

    private void setFacility(Facility facility) {
        for (Schedule schedule : schedules) {
            schedule.setFacility(facility);
        }
    }

    @Override
    public String toString() {
        return "FacilitySchedules{" +
            "facilitySchedules=" + schedules +
            '}';
    }
}
