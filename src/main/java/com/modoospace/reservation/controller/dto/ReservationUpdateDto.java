package com.modoospace.reservation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modoospace.common.DateFormatManager;
import com.modoospace.reservation.domain.DateTimeRange;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationStatus;
import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationUpdateDto {

    @NotNull
    private Integer numOfUser;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatManager.DATE_FORMAT)
    private LocalDate startDate;

    private Integer startHour;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatManager.DATE_FORMAT)
    private LocalDate endDate;

    private Integer endHour;

    private ReservationStatus status;

    @Builder
    public ReservationUpdateDto(Integer numOfUser, LocalDate startDate, Integer startHour,
        LocalDate endDate, Integer endHour, ReservationStatus status) {
        this.numOfUser = numOfUser;
        this.startDate = startDate;
        this.startHour = startHour;
        this.endDate = endDate;
        this.endHour = endHour;
        this.status = status;
    }

    public Reservation toEntity(Reservation reservation) {
        return Reservation.builder()
            .numOfUser(numOfUser)
            .dateTimeRange(new DateTimeRange(startDate, startHour, endDate, endHour))
            .status(status)
            .visitor(reservation.getVisitor())
            .facility(reservation.getFacility())
            .build();
    }
}
