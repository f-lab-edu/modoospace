package com.modoospace.reservation.controller.dto;

import com.modoospace.reservation.domain.Reservation;
import com.modoospace.space.domain.Schedule;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
public class TimeResponse {

    private final Integer hour;
    private final Boolean available;

    public TimeResponse(Integer hour, Boolean available) {
        this.hour = hour;
        this.available = available;
    }

    public static List<TimeResponse> createTimeResponse(List<Schedule> schedules, List<Reservation> reservations, LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate nowDate = now.toLocalDate();
        int nowHour = now.getHour();

        return IntStream.range(0, 24)
                .mapToObj(hour -> {
                    if (date.isBefore(nowDate)) {
                        return new TimeResponse(hour, false);
                    }

                    if (date.equals(nowDate) && hour <= nowHour) {
                        return new TimeResponse(hour, false);
                    }

                    Boolean isAvailable = checkSchedules(schedules, hour);
                    Boolean isReserved = checkReservations(reservations, date, hour);
                    return new TimeResponse(hour, isAvailable && !isReserved);
                })
                .collect(Collectors.toList());
    }

    private static Boolean checkSchedules(List<Schedule> schedules, int hour) {
        for (Schedule schedule : schedules) {
            if (schedule.isBetween(hour)) {
                return true;
            }
        }
        return false;
    }

    private static Boolean checkReservations(List<Reservation> reservations, LocalDate date, int hour) {
        for (Reservation reservation : reservations) {
            if (reservation.isBetween(date, hour)) {
                return true;
            }
        }
        return false;
    }
}
