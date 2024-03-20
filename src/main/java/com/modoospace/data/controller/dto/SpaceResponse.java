package com.modoospace.data.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.modoospace.data.controller.dto.space.*;
import com.modoospace.member.domain.Member;
import com.modoospace.space.domain.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpaceResponse {

    @JsonProperty("products")
    private List<FacilityResponse> facilityResponses = new ArrayList<>();

    @JsonProperty("info")
    private SpaceInfo spaceInfo;

    private Location location;

    @JsonProperty("break_days")
    private List<BreakDay> breakDays = new ArrayList<>();

    @JsonProperty("break_holidays")
    private List<BreakHoliday> breakHolidays = new ArrayList<>();

    @JsonProperty("break_times")
    private List<BreakTime> breakTimes = new ArrayList<>();

    public String getCategoryName() {
        return facilityResponses.stream()
                .findFirst()
                .flatMap(p -> p.getCategories().stream().findFirst())
                .map(FacilityCategory::getName)
                .get();
    }

    public Space toSpace(Address address, Category category, Member member) {
        return Space.builder()
                .name(spaceInfo.getName())
                .description(spaceInfo.getDescription())
                .address(address)
                .category(category)
                .host(member)
                .build();
    }

    public WeekdaySettings getWeekdaySettings() {
        List<WeekdaySetting> weekdaySettings = new ArrayList<>(Arrays.asList(
                new WeekdaySetting(DayOfWeek.MONDAY),
                new WeekdaySetting(DayOfWeek.TUESDAY),
                new WeekdaySetting(DayOfWeek.WEDNESDAY),
                new WeekdaySetting(DayOfWeek.THURSDAY),
                new WeekdaySetting(DayOfWeek.FRIDAY))
        );

        if (breakDays.isEmpty() && breakHolidays.isEmpty()) {
            weekdaySettings.add(new WeekdaySetting(DayOfWeek.SATURDAY));
            weekdaySettings.add(new WeekdaySetting(DayOfWeek.SUNDAY));
        }

        return new WeekdaySettings(weekdaySettings);
    }

    public TimeSettings getTimeSettings() {
        List<TimeSetting> timeSettings = breakTimes.stream()
                .flatMap(breakTime -> makeTimeRangeFromBreakTime(breakTime).stream())
                .map(TimeSetting::new)
                .collect(Collectors.toList());

        return new TimeSettings(timeSettings);
    }

    // end 10 ~ start 2
    private static List<TimeRange> makeTimeRangeFromBreakTime(BreakTime breakTime) {
        List<TimeRange> list = new ArrayList<>();
        if(breakTime.getEndHour() > breakTime.getStartHour()) {
            list.add(new TimeRange(breakTime.getEndHour(), 24));
            list.add(new TimeRange(0, breakTime.getStartHour()));
        }
        else {
            list.add(new TimeRange(breakTime.getEndHour(), breakTime.getStartHour()));
        }
        return list;
    }
}
