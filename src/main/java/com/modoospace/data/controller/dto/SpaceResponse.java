package com.modoospace.data.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.modoospace.data.controller.dto.space.*;
import com.modoospace.member.domain.Member;
import com.modoospace.space.domain.*;
import lombok.Getter;
import lombok.Setter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpaceResponse {

    private List<Product> products;
    private SpaceInfo info;
    private Location location;
    private List<BreakDay> break_days;
    private List<BreakHoliday> break_holidays;
    private List<BreakTime> break_times;

    public Optional<String> getEncodedAddress() {
        return Optional.ofNullable(location)
                .map(Location::getAddress)
                .map(address -> URLEncoder.encode(address, StandardCharsets.UTF_8));
    }

    public Optional<String> getCategoryName() {
        return products.stream()
                .findFirst()
                .flatMap(p -> p.getCategories().stream().findFirst())
                .map(FacilityCategory::getName);
    }

    public Space toSpace(Address address, Category category, Member member) {
        return Space.builder()
                .name(info.getName())
                .description(info.getDescription())
                .address(address)
                .category(category)
                .host(member)
                .build();
    }

    public List<Facility> toFacilities(Space space) {
        return null;
    }

    public WeekdaySettings getWeekdaySettings() {
        List<WeekdaySetting> weekdaySettings = new ArrayList<>(Arrays.asList(
                new WeekdaySetting(DayOfWeek.MONDAY),
                new WeekdaySetting(DayOfWeek.TUESDAY),
                new WeekdaySetting(DayOfWeek.WEDNESDAY),
                new WeekdaySetting(DayOfWeek.THURSDAY),
                new WeekdaySetting(DayOfWeek.FRIDAY))
        );

        if (break_days.isEmpty() && break_holidays.isEmpty()) {
            weekdaySettings.add(new WeekdaySetting(DayOfWeek.SATURDAY));
            weekdaySettings.add(new WeekdaySetting(DayOfWeek.SUNDAY));
        }

        return new WeekdaySettings(weekdaySettings);
    }

    public TimeSettings getTimeSettings() {
        List<TimeSetting> timeSettings = break_times.stream()
                .map(breakTime -> new TimeRange(breakTime.getStartHour(), breakTime.getEndHour()))
                .map(TimeSetting::new)
                .collect(Collectors.toList());

        return new TimeSettings(timeSettings);
    }
}
