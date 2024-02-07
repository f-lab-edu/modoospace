package com.modoospace.reservation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modoospace.common.DateFormatManager;
import com.modoospace.member.domain.Member;
import com.modoospace.reservation.domain.DateTimeRange;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.space.domain.Facility;
import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationCreateRequest {

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

    @Builder
    public ReservationCreateRequest(Integer numOfUser, LocalDate startDate, Integer startHour,
        LocalDate endDate, Integer endHour) {
        this.numOfUser = numOfUser;
        this.startDate = startDate;
        this.startHour = startHour;
        this.endDate = endDate;
        this.endHour = endHour;
    }

    public Reservation toEntity(Facility facility, Member visitor) {
        return Reservation.builder()
            .numOfUser(numOfUser)
            .dateTimeRange(new DateTimeRange(startDate, startHour, endDate, endHour))
            .status(ReservationStatus.WAITING)
            .visitor(visitor)
            .facility(facility)
            .build();
    }
}
