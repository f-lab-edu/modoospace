package com.modoospace.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import com.modoospace.alarm.producer.AlarmProducer;
import com.modoospace.common.exception.ConflictingReservationException;
import com.modoospace.common.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.member.service.MemberService;
import com.modoospace.reservation.controller.dto.AvailabilityTimeDto;
import com.modoospace.reservation.controller.dto.ReservationCreateDto;
import com.modoospace.reservation.controller.dto.ReservationReadDto;
import com.modoospace.reservation.domain.ReservationRepository;
import com.modoospace.reservation.domain.ReservationStatus;
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.reservation.serivce.ReservationService;
import com.modoospace.space.controller.dto.facility.FacilityCreateDto;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateDto;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilityRepository;
import com.modoospace.space.domain.ScheduleRepository;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import com.modoospace.space.repository.ScheduleQueryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReservationServiceTest {

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

    private Facility facility1;

    private Facility facility2;

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
        memberRepository.save(hostMember);

        visitorMember = Member.builder()
            .email("visitor@email")
            .name("visitor")
            .role(Role.VISITOR)
            .build();
        memberRepository.save(visitorMember);

        Category category = Category.builder()
            .name("스터디 공간")
            .build();
        categoryRepository.save(category);

        SpaceCreateUpdateDto spaceCreateDto = SpaceCreateUpdateDto.builder()
            .name("공간이름")
            .description("설명")
            .build();
        Space space = spaceCreateDto.toEntity(category, hostMember);
        spaceRepository.save(space);

        FacilityCreateDto createRoomDto1 = FacilityCreateDto.builder()
            .name("스터디룸1")
            .reservationEnable(true)
            .minUser(1)
            .maxUser(4)
            .description("1~4인실 입니다.")
            .timeSettings(
                Arrays.asList(new TimeSettingCreateDto(9, 24))
            )
            .build();
        facility1 = facilityRepository.save(createRoomDto1.toEntity(space));

        FacilityCreateDto createRoomDto2 = FacilityCreateDto.builder()
            .name("스터디룸2")
            .reservationEnable(true)
            .minUser(3)
            .maxUser(6)
            .description("3~6인실 입니다.")
            .timeSettings(
                Arrays.asList(new TimeSettingCreateDto(0, 24))
            )
            .build();
        facility2 = facilityRepository.save(createRoomDto2.toEntity(space));

        now = LocalDate.now();
    }

    @DisplayName("예약가능한 시간을 조회한다.(9시~24시)")
    @Test
    public void getAvailabilityTime() {
        AvailabilityTimeDto retDto = reservationService.getAvailabilityTime(facility1.getId(), now);

        // 9~23
        Assertions.assertThat(retDto.getTimeResponses().stream()
                .filter(timeResponse -> timeResponse.getAvailable()))
            .hasSize(15);
    }

    @DisplayName("예약가능한 시간을 조회한다.(0시~24시)")
    @Test
    public void getAvailabilityTime_24Open() {
        AvailabilityTimeDto retDto = reservationService.getAvailabilityTime(facility2.getId(),
            LocalDate.now());

        // 0~23
        Assertions.assertThat(retDto.getTimeResponses().stream()
                .filter(timeResponse -> timeResponse.getAvailable()))
            .hasSize(24);
    }

    @DisplayName("특정 날짜의 예약가능시간을 조회할 수 있다.(12~15시 예약존재)")
    @Test
    public void getAvailableTimes_ifPresentReservation() {
        ReservationCreateDto createDto = new ReservationCreateDto(3, now, 12, now, 15);

        reservationService.createReservation(createDto, facility1.getId(),
            visitorMember.getEmail());

        AvailabilityTimeDto retDto = reservationService.getAvailabilityTime(facility1.getId(),
            LocalDate.now());

        // 9시 ~ 12시, 15시 ~ 24시
        Assertions.assertThat(retDto.getTimeResponses().stream()
                .filter(timeResponse -> timeResponse.getAvailable()))
            .hasSize(15 - 3);
    }

    @DisplayName("Visitor는 예약을 생성할 수 있다.")
    @Test
    public void createReservation_IfVisitor() {
        ReservationCreateDto createDto = new ReservationCreateDto(3, now, 18, now, 21);

        Long reservationId = reservationService.createReservation(createDto, facility1.getId(),
            visitorMember.getEmail());

        ReservationReadDto readDto = reservationService.findReservation(reservationId,
            visitorMember.getEmail());
        assertAll(
            () -> assertThat(readDto.getId()).isEqualTo(reservationId),
            () -> assertThat(readDto.getFacility().getId()).isEqualTo(facility1.getId()),
            () -> assertThat(readDto.getMember().getId()).isEqualTo(visitorMember.getId()),
            () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.WAITING)
        );
    }

    @DisplayName("기존 예약과 시간이 겹친다면 Room은 예약할 수 없다.")
    @Test
    public void createReservationRoom_throwException_ifOverlappingReservation() {
        ReservationCreateDto createDto = new ReservationCreateDto(3, now, 12, now, 15);
        reservationService.createReservation(createDto, facility1.getId(), visitorMember.getEmail());

        ReservationCreateDto createDto2 = new ReservationCreateDto(3, now, 13, now, 15);
        assertThatThrownBy(() -> reservationService
            .createReservation(createDto2, facility1.getId(), visitorMember.getEmail()))
            .isInstanceOf(ConflictingReservationException.class);
    }

    @DisplayName("방문자가 본인의 예약을 취소할 수 있다.")
    @Test
    public void cancelReservation() {
        ReservationCreateDto createDto = new ReservationCreateDto(3, now, 12, now, 15);
        Long reservationId = reservationService
            .createReservation(createDto, facility1.getId(), visitorMember.getEmail());

        reservationService.cancelReservation(reservationId, visitorMember.getEmail());

        ReservationReadDto readDto = reservationService
            .findReservation(reservationId, visitorMember.getEmail());
        assertAll(
            () -> assertThat(readDto.getId()).isEqualTo(reservationId),
            () -> assertThat(readDto.getMember().getEmail()).isEqualTo(visitorMember.getEmail()),
            () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.CANCELED)
        );
    }

    @DisplayName("예약을 생성한 사용자가 아닌 다른 사용자가 해당 예약을 취소하려고 한다면 예외가 발생한다")
    @Test
    public void cancelReservation_throwException_IfNotMyReservation() {
        ReservationCreateDto createDto = new ReservationCreateDto(3, now, 12, now, 15);
        Long reservationId = reservationService
            .createReservation(createDto, facility1.getId(), visitorMember.getEmail());

        assertAll(
            () -> assertThatThrownBy(
                () -> reservationService.cancelReservation(reservationId, hostMember.getEmail()))
                .isInstanceOf(PermissionDeniedException.class)
        );
    }
}
