package com.modoospace.reservation.domain;

import com.modoospace.common.BaseTimeEntity;
import com.modoospace.common.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import com.modoospace.space.domain.Facility;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "reservation_id")
    private Long id;

    @Column(nullable = false)
    private Integer numOfUser;

    @NotNull
    private DateTimeRange dateTimeRange;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_id")
    private Member visitor;


    @Builder
    public Reservation(Long id, Integer numOfUser, DateTimeRange dateTimeRange,
        ReservationStatus status, Facility facility, Member visitor) {
        this.id = id;

        facility.verityNumOfUser(numOfUser);
        this.numOfUser = numOfUser;

        this.dateTimeRange = dateTimeRange;
        this.status = status == null ? ReservationStatus.WAITING : status;

        facility.verifyReservationEnable();
        this.facility = facility;

        visitor.verifyRolePermission(Role.VISITOR);
        this.visitor = visitor;
    }

    public void approveAsHost(Member loginMember) {
        verifyManagementPermission(loginMember);
        this.status = ReservationStatus.COMPLETED;
    }

    public void updateAsHost(final Reservation updateReservation, Member loginMember) {
        verifyManagementPermission(loginMember);
        this.numOfUser = updateReservation.getNumOfUser();
        this.dateTimeRange.update(updateReservation.getDateTimeRange());
        this.status = updateReservation.getStatus();
    }

    public void cancelAsVisitor(Member loginMember) {
        verifySameVisitor(loginMember);
        this.status = ReservationStatus.CANCELED;
    }

    public void verifyReservationAccess(Member loginMember) {
        if (loginMember.isSameRole(Role.VISITOR)) {
            verifySameVisitor(loginMember);
            return;
        }
        verifyManagementPermission(loginMember);
    }

    private void verifySameVisitor(Member loginMember) {
        if (!visitor.getId().equals(loginMember.getId())) {
            throw new PermissionDeniedException();
        }
    }

    private void verifyManagementPermission(Member loginMember) {
        facility.verifyManagementPermission(loginMember);
    }

    public boolean isBetween(LocalDate date, Integer hour) {
        return isConflictingRange(new DateTimeRange(date, hour, date, hour + 1));
    }

    public boolean isConflictingRange(DateTimeRange dateTimeRange) {
        return this.dateTimeRange.isConflicting(dateTimeRange);
    }

    public LocalDate getStartDate() {
        return this.dateTimeRange.getStartDate();
    }

    public Integer getStartHour() {
        return this.dateTimeRange.getStartHour();
    }

    public LocalDate getEndDate() {
        return this.dateTimeRange.getEndDate();
    }

    public Integer getEndHour() {
        return this.dateTimeRange.getEndHour();
    }

    public Member getHost() {
        return this.facility.getSpace().getHost();
    }
}
