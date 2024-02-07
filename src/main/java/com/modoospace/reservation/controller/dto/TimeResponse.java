package com.modoospace.reservation.controller.dto;

import com.modoospace.reservation.domain.Reservation;
import com.modoospace.space.domain.Schedule;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;

@Getter
public class TimeResponse {

    private Integer hour;
    private Boolean available;

    public TimeResponse(Integer hour, Boolean available) {
        this.hour = hour;
        this.available = available;
    }

    public static List<TimeResponse> createTimeResponse(List<Schedule> schedules,
        List<Reservation> reservations, LocalDate date) {
        return IntStream.range(0, 24)
            .mapToObj(hour -> {
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

    private static Boolean checkReservations(List<Reservation> reservations, LocalDate date,
        int hour) {
        for (Reservation reservation : reservations) {
            if (reservation.isBetween(date, hour)) {
                return true;
            }
        }
        return false;
    }
}
