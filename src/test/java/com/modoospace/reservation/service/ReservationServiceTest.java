package com.modoospace.reservation.service;

import com.modoospace.AbstractIntegrationContainerBaseTest;
import com.modoospace.alarm.producer.AlarmProducer;
import com.modoospace.common.exception.ConflictingReservationException;
import com.modoospace.common.exception.NotOpenedFacilityException;
import com.modoospace.common.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.member.service.MemberService;
import com.modoospace.reservation.controller.dto.AvailabilityTimeResponse;
import com.modoospace.reservation.controller.dto.ReservationCreateRequest;
import com.modoospace.reservation.controller.dto.ReservationResponse;
import com.modoospace.reservation.controller.dto.TimeResponse;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.reservation.serivce.ReservationService;
import com.modoospace.space.controller.dto.facility.FacilityCreateRequest;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateRequest;
import com.modoospace.space.domain.*;
import com.modoospace.space.repository.ScheduleQueryRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

@Transactional
public class ReservationServiceTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    private Member visitorMember;

    private Member hostMember;

    private Facility facilityOpen9Close24;

    private Facility facilityOpenClose24;

    private Facility facilityNotAvailable;

    private ReservationService reservationService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleQueryRepository scheduleQueryRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationQueryRepository reservationQueryRepository;

    private LocalDate now;

    @BeforeEach
    public void setUp() {
        AlarmProducer alarmProducerMock = mock(AlarmProducer.class);
        reservationService = new ReservationService(memberService, facilityRepository,
                scheduleRepository, scheduleQueryRepository, reservationRepository,
                reservationQueryRepository, alarmProducerMock);

        hostMember = Member.builder()
                .email("host@email")
                .name("host")
                .role(Role.HOST)
                .build();
        hostMember = memberRepository.save(hostMember);

        visitorMember = Member.builder()
                .email("visitor@email")
                .name("visitor")
                .role(Role.VISITOR)
                .build();
        visitorMember = memberRepository.save(visitorMember);

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
        FacilityCreateRequest createRequest1 = FacilityCreateRequest.builder()
                .name("스터디룸")
                .reservationEnable(true)
                .minUser(1)
                .maxUser(4)
                .description("1~4인실 입니다.")
                .timeSettings(
                        List.of(new TimeSettingCreateRequest(9, 24))
                )
                .build();
        facilityOpen9Close24 = facilityRepository.save(createRequest1.toEntity(space));

        FacilityCreateRequest createRequest2 = FacilityCreateRequest.builder()
                .name("스터디룸2")
                .reservationEnable(true)
                .minUser(3)
                .maxUser(6)
                .description("3~6인실 입니다.")
                .build();
        facilityOpenClose24 = facilityRepository.save(createRequest2.toEntity(space));

        FacilityCreateRequest createRequest3 = FacilityCreateRequest.builder()
                .name("스터디룸3")
                .reservationEnable(false)
                .minUser(3)
                .maxUser(6)
                .description("3~6인실 입니다.")
                .build();
        facilityNotAvailable = facilityRepository.save(createRequest3.toEntity(space));

        now = LocalDate.now();
    }

    @DisplayName("Visitor는 예약을 생성할 수 있다.")
    @Test
    public void createReservation_IfVisitor() {
        ReservationCreateRequest createRequest = new ReservationCreateRequest(3, now, 18, now, 21);

        Long reservationId = reservationService.createReservation(createRequest, facilityOpen9Close24.getId(),
                visitorMember);

        ReservationResponse retResponse = reservationService.findReservation(reservationId,
                visitorMember);
        assertAll(
                () -> assertThat(retResponse.getId()).isEqualTo(reservationId),
                () -> assertThat(retResponse.getFacility().getId()).isEqualTo(facilityOpen9Close24.getId()),
                () -> assertThat(retResponse.getVisitor().getId()).isEqualTo(visitorMember.getId()),
                () -> assertThat(retResponse.getStatus()).isEqualTo(ReservationStatus.WAITING)
        );
    }

    @DisplayName("기존 예약과 시간이 겹친다면 Room은 예약할 수 없다.")
    @Test
    public void createReservationRoom_throwException_ifOverlappingReservation() {
        ReservationCreateRequest createRequest = new ReservationCreateRequest(3, now, 12, now, 15);
        reservationService.createReservation(createRequest, facilityOpen9Close24.getId(),
                visitorMember);

        ReservationCreateRequest createRequest2 = new ReservationCreateRequest(3, now, 13, now, 15);
        assertThatThrownBy(() -> reservationService
                .createReservation(createRequest2, facilityOpen9Close24.getId(), visitorMember))
                .isInstanceOf(ConflictingReservationException.class);
    }

    @DisplayName("예약이 불가능한 Room은 예약할 수 없다.")
    @Test
    public void createReservationRoom_throwException_ifNotAvailable() {
        ReservationCreateRequest createRequest = new ReservationCreateRequest(3, now, 12, now, 15);
        assertThatThrownBy(() -> reservationService.createReservation(createRequest, facilityNotAvailable.getId(),
                visitorMember))
                .isInstanceOf(NotOpenedFacilityException.class);
    }

    @DisplayName("Open되지 않은 시간은 예약할 수 없다.")
    @Test
    public void createReservationRoom_throwException_ifNotOpen() {
        ReservationCreateRequest createRequest = new ReservationCreateRequest(3, now, 6, now, 9);
        assertThatThrownBy(() -> reservationService.createReservation(createRequest, facilityOpen9Close24.getId(),
                visitorMember))
                .isInstanceOf(NotOpenedFacilityException.class);
    }

    @DisplayName("예약가능한 시간을 조회한다.(9시~24시)")
    @Test
    public void getAvailabilityTime() {
        AvailabilityTimeResponse retResponse = reservationService.getAvailabilityTime(facilityOpen9Close24.getId(),
                now.plusDays(1));

        // 9~23
        Assertions.assertThat(retResponse.getTimeResponses().stream()
                        .filter(TimeResponse::getAvailable))
                .hasSize(15);
    }

    @DisplayName("예약가능한 시간을 조회한다.(0시~24시)")
    @Test
    public void getAvailabilityTime_24Open() {
        AvailabilityTimeResponse retResponse = reservationService.getAvailabilityTime(facilityOpenClose24.getId(),
                LocalDate.now().plusDays(1));

        // 0~23
        Assertions.assertThat(retResponse.getTimeResponses().stream()
                        .filter(TimeResponse::getAvailable))
                .hasSize(24);
    }

    @DisplayName("특정 날짜의 예약가능시간을 조회할 수 있다.(12~15시 예약존재)")
    @Test
    public void getAvailableTimes_ifPresentReservation() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ReservationCreateRequest createRequest = new ReservationCreateRequest(3, tomorrow, 12, tomorrow, 15);

        reservationService.createReservation(createRequest, facilityOpen9Close24.getId(),
                visitorMember);

        AvailabilityTimeResponse retResponse = reservationService.getAvailabilityTime(facilityOpen9Close24.getId(), tomorrow);

        // 9시 ~ 12시, 15시 ~ 24시
        Assertions.assertThat(retResponse.getTimeResponses().stream()
                        .filter(TimeResponse::getAvailable))
                .hasSize(15 - 3);
    }


    @DisplayName("본인이 생성한 예약을 조회할 수 있다.")
    @Test
    public void findReservation_ByVisitor() {
        ReservationCreateRequest createRequest = new ReservationCreateRequest(3, now, 12, now, 15);
        Long reservationId = reservationService
                .createReservation(createRequest, facilityOpen9Close24.getId(), visitorMember);

        ReservationResponse retResponse = reservationService.findReservation(reservationId, visitorMember);

        assertAll(
                () -> assertThat(retResponse.getId()).isEqualTo(reservationId),
                () -> assertThat(retResponse.getVisitor().getEmail()).isEqualTo(visitorMember.getEmail())
        );
    }

    @DisplayName("호스트는 본인의 시설에 생성된 예약을 조회할 수 있다.")
    @Test
    public void findReservation_ByHost() {
        ReservationCreateRequest createRequest = new ReservationCreateRequest(3, now, 12, now, 15);
        Long reservationId = reservationService
                .createReservation(createRequest, facilityOpen9Close24.getId(), visitorMember);

        ReservationResponse retResponse = reservationService.findReservation(reservationId, hostMember);

        assertAll(
                () -> assertThat(retResponse.getId()).isEqualTo(reservationId),
                () -> assertThat(retResponse.getVisitor().getEmail()).isEqualTo(visitorMember.getEmail())
        );
    }

    @DisplayName("호스트는 본인의 시설에 생성된 예약을 승인할 수 있다.")
    @Test
    public void approveReservation_ByHost() {
        ReservationCreateRequest createRequest = new ReservationCreateRequest(3, now, 12, now, 15);
        Long reservationId = reservationService
                .createReservation(createRequest, facilityOpen9Close24.getId(), visitorMember);

        reservationService.approveReservation(reservationId, hostMember);

        ReservationResponse retResponse = reservationService.findReservation(reservationId, hostMember);
        assertAll(
                () -> assertThat(retResponse.getId()).isEqualTo(reservationId),
                () -> assertThat(retResponse.getVisitor().getEmail()).isEqualTo(visitorMember.getEmail()),
                () -> assertThat(retResponse.getStatus()).isEqualTo(ReservationStatus.COMPLETED)
        );
    }

    @DisplayName("호스트가 아닌자가 해당 예약을 승인하려 한다면 예외가 발생한다")
    @Test
    public void approveReservation_throwException_IfNotHost() {
        ReservationCreateRequest createRequest = new ReservationCreateRequest(3, now, 12, now, 15);
        Long reservationId = reservationService
                .createReservation(createRequest, facilityOpen9Close24.getId(), visitorMember);

        reservationService.approveReservation(reservationId, hostMember);

        assertThatThrownBy(
                () -> reservationService.approveReservation(reservationId, visitorMember))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @DisplayName("방문자가 본인의 예약을 취소할 수 있다.")
    @Test
    public void cancelReservation_ByVisitor() {
        ReservationCreateRequest createRequest = new ReservationCreateRequest(3, now, 12, now, 15);
        Long reservationId = reservationService
                .createReservation(createRequest, facilityOpen9Close24.getId(), visitorMember);

        reservationService.cancelReservation(reservationId, visitorMember);

        ReservationResponse retResponse = reservationService.findReservation(reservationId, visitorMember);
        assertAll(
                () -> assertThat(retResponse.getId()).isEqualTo(reservationId),
                () -> assertThat(retResponse.getVisitor().getEmail()).isEqualTo(visitorMember.getEmail()),
                () -> assertThat(retResponse.getStatus()).isEqualTo(ReservationStatus.CANCELED)
        );
    }

    @DisplayName("예약을 생성한 사용자가 아닌 다른 사용자가 해당 예약을 취소하려고 한다면 예외가 발생한다")
    @Test
    public void cancelReservation_throwException_IfNotMyReservation() {
        ReservationCreateRequest createRequest = new ReservationCreateRequest(3, now, 12, now, 15);
        Long reservationId = reservationService
                .createReservation(createRequest, facilityOpen9Close24.getId(), visitorMember);

        assertThatThrownBy(
                () -> reservationService.cancelReservation(reservationId, hostMember))
                .isInstanceOf(PermissionDeniedException.class);
    }
}
