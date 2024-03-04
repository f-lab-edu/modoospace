package com.modoospace.reservation.repository;

import com.modoospace.JpaTestConfig;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.reservation.domain.DateTimeRange;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.space.controller.dto.facility.FacilityCreateRequest;
import com.modoospace.space.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaTestConfig.class)
@ActiveProfiles("test")
public class ReservationQueryRepositoryTest {

    @Autowired
    private ReservationQueryRepository reservationQueryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Member visitorMember;

    private Facility facility;

    private LocalDate now;

    @BeforeEach
    public void setUp() {
        Member hostMember = Member.builder()
                .email("host@email")
                .name("host")
                .role(Role.HOST)
                .build();
        memberRepository.save(hostMember);

        visitorMember = Member.builder()
                .email("visitor@email")
                .name("visitor")
                .role(Role.VISITOR)
                .build();
        memberRepository.save(visitorMember);

        Category category = new Category("스터디 공간");
        categoryRepository.save(category);

        Space space = Space.builder()
                .name("공간이름")
                .description("설명")
                .category(category)
                .host(hostMember)
                .build();
        spaceRepository.save(space);

        // TimeSetting, WeekSetting 기본값이 필요하여 Request 사용.
        FacilityCreateRequest createRequest = FacilityCreateRequest.builder()
                .name("스터디룸")
                .reservationEnable(true)
                .minUser(1)
                .maxUser(4)
                .description("1~4인실 입니다.")
                .build();
        facility = facilityRepository.save(createRequest.toEntity(space));

        now = LocalDate.now();
    }

    @Test
    @DisplayName("해당 날짜에 존재하는 활성화된 예약을 반환한다.")
    public void findActiveReservations_returnActiveReservationList() {
        DateTimeRange dateTimeRange = new DateTimeRange(now, 13, now, 16);
        Reservation reservation1 = Reservation.builder()
                .numOfUser(3)
                .dateTimeRange(dateTimeRange)
                .visitor(visitorMember)
                .facility(facility)
                .build();
        reservationRepository.save(reservation1);
        DateTimeRange dateTimeRange2 = new DateTimeRange(now, 22, now.plusDays(1), 2);
        Reservation reservation2 = Reservation.builder()
                .numOfUser(3)
                .dateTimeRange(dateTimeRange2)
                .visitor(visitorMember)
                .facility(facility)
                .build();
        reservationRepository.save(reservation2);

        List<Reservation> activeReservations = reservationQueryRepository.findActiveReservations(
                facility, now);

        assertThat(activeReservations).hasSize(2);
    }

    @Test
    @DisplayName("해당 날짜에 존재하는 활성화된 예약이 없다면 빈List를 반환한다.")
    public void findActiveReservations_returnEmptyList() {
        List<Reservation> activeReservations = reservationQueryRepository.findActiveReservations(
                facility, now);

        assertThat(activeReservations).isEmpty();
    }


    @Test
    @DisplayName("동일한 시설,시간에 기존 예약이 있다면 true를 반환한다.")
    public void isConflictingReservation_returnTrue() {
        DateTimeRange dateTimeRange = new DateTimeRange(now, 13, now, 16);
        Reservation reservation1 = Reservation.builder()
                .numOfUser(3)
                .dateTimeRange(dateTimeRange)
                .visitor(visitorMember)
                .facility(facility)
                .build();
        reservationRepository.save(reservation1);

        Boolean isExist = reservationQueryRepository
                .isConflictingReservation(facility, dateTimeRange);
        assertThat(isExist).isTrue();
    }

    @Test
    @DisplayName("동일한 시설,시간에 기존 예약이 있다면 true를 반환한다.")
    public void isConflictingReservation_returnFalse() {
        DateTimeRange dateTimeRange = new DateTimeRange(now, 13, now, 16);
        Reservation reservation1 = Reservation.builder()
                .numOfUser(3)
                .dateTimeRange(dateTimeRange)
                .visitor(visitorMember)
                .facility(facility)
                .build();
        reservationRepository.save(reservation1);

        dateTimeRange = new DateTimeRange(now, 16, now, 17);
        Boolean isExist = reservationQueryRepository
                .isConflictingReservation(facility, dateTimeRange);
        assertThat(isExist).isFalse();
    }
}
