package com.modoospace.reservation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modoospace.common.DateFormatManager;
import com.modoospace.member.controller.dto.MemberReadDto;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.space.controller.dto.facility.FacilityReadDto;
import java.time.LocalDate;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationReadDto {

    @NotNull
    private Long id;

    @NotNull
    private Integer numOrUser;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatManager.DATE_FORMAT)
    private LocalDate startDate;

    private Integer startHour;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatManager.DATE_FORMAT)
    private LocalDate endDate;

    private Integer endHour;

    @NotEmpty
    private ReservationStatus status;

    @NotEmpty
    private FacilityReadDto facility;

    @NotEmpty
    private MemberReadDto member;

    @Builder
    public ReservationReadDto(Long id, Integer numOrUser, LocalDate startDate, Integer startHour,
        LocalDate endDate, Integer endHour, ReservationStatus status, FacilityReadDto facility,
        MemberReadDto member) {
        this.id = id;
        this.numOrUser = numOrUser;
        this.startDate = startDate;
        this.startHour = startHour;
        this.endDate = endDate;
        this.endHour = endHour;
        this.status = status;
        this.facility = facility;
        this.member = member;
    }


    public static ReservationReadDto toDto(Reservation reservation) {
        return ReservationReadDto.builder()
            .id(reservation.getId())
            .numOrUser(reservation.getNumOfUser())
            .startDate(reservation.getStartDate())
            .startHour(reservation.getStartHour())
            .endDate(reservation.getEndDate())
            .endHour(reservation.getEndHour())
            .status(reservation.getStatus())
            .facility(FacilityReadDto.toDto(reservation.getFacility()))
            .member(MemberReadDto.toDto(reservation.getVisitor()))
            .build();
    }
}
