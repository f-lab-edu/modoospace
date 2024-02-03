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
import com.modoospace.reservation.controller.dto.AvailabilityTimeResponseDto;
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
import com.modoospace.space.domain.FacilityScheduleRepository;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import com.modoospace.space.repository.FacilityScheduleQueryRepository;
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

    private Facility roomFacility1;

    private Facility roomFacility2;

    private LocalDateTime now;

    private ReservationService reservationService;

    // TODO : AlarmProducer mock객체를 Injection하기 위해 테스트에 사용되지 않는
    //        하지만 ReservationService에 결합된 클래스들을 가져오는게 맞는걸까?
    @Autowired
    private MemberService memberService;

    @Autowired
    private FacilityScheduleRepository facilityScheduleRepository;

    @Autowired
    private FacilityScheduleQueryRepository facilityScheduleQueryRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationQueryRepository reservationQueryRepository;

    @BeforeEach
    public void setUp() {
        AlarmProducer alarmProducerMock = mock(AlarmProducer.class);
        reservationService = new ReservationService(memberService, facilityRepository,
            facilityScheduleRepository, facilityScheduleQueryRepository, reservationRepository,
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
            .description("1~4인실 입니다.")
            .reservationEnable(true)
            .timeSettings(Arrays
                .asList(new TimeSettingCreateDto(LocalTime.of(9, 0, 0), LocalTime.of(23, 59, 59))))
            .build();
        roomFacility1 = facilityRepository.save(createRoomDto1.toEntity(space));

        FacilityCreateDto createRoomDto2 = FacilityCreateDto.builder()
            .name("스터디룸2")
            .description("3~6인실 입니다.")
            .reservationEnable(true)
            .timeSettings(Arrays
                .asList(new TimeSettingCreateDto(LocalTime.of(0, 0, 0), LocalTime.of(23, 59, 59))))
            .build();
        roomFacility2 = facilityRepository.save(createRoomDto2.toEntity(space));

        now = LocalDateTime.now();
    }

    @DisplayName("특정 날짜의 예약가능시간을 조회할 수 있다.")
    @Test
    public void getAvailableTimes() {
        AvailabilityTimeResponseDto retDto = reservationService
            .getAvailabilityTime(roomFacility1.getId(), now.toLocalDate());

        // 09:00:00 ~ 23:59:59
        Assertions.assertThat(retDto.getAvailableTimes()).hasSize(15);
    }

    @DisplayName("특정 날짜의 예약가능시간을 조회할 수 있다.(24시간)")
    @Test
    public void getAvailableTimes_24Open() {
        AvailabilityTimeResponseDto retDto = reservationService
            .getAvailabilityTime(roomFacility2.getId(), now.toLocalDate());

        // 00:00:00 ~ 23:59:59
        Assertions.assertThat(retDto.getAvailableTimes()).hasSize(24);
    }

    @DisplayName("특정 날짜의 예약가능시간을 조회할 수 있다.(12~15시 예약존재)")
    @Test
    public void getAvailableTimes_ifPresentReservation() {
        LocalDateTime reservationStart = now.toLocalDate().atTime(LocalTime.of(12, 0, 0));
        LocalDateTime reservationEnd = now.toLocalDate().atTime(LocalTime.of(14, 59, 59));
        ReservationCreateDto dto = new ReservationCreateDto(reservationStart, reservationEnd);

        reservationService.createReservation(dto, roomFacility1.getId(), visitorMember.getEmail());

        AvailabilityTimeResponseDto retDto = reservationService
            .getAvailabilityTime(roomFacility1.getId(), now.toLocalDate());

        // 09:00:00 ~ 11:59:59, 15:00:00 ~ 23:59:59
        Assertions.assertThat(retDto.getAvailableTimes()).hasSize(15 - 3);
    }

    @DisplayName("로그인한 멤버가 비지터일 경우 예약을 생성할 수 있다.")
    @Test
    public void createReservation_IfVisitor() {
        LocalDateTime reservationStart = now.toLocalDate().atTime(LocalTime.of(12, 0, 0));
        LocalDateTime reservationEnd = now.toLocalDate().atTime(LocalTime.of(14, 59, 59));
        ReservationCreateDto dto = new ReservationCreateDto(reservationStart, reservationEnd);
        Long reservationId = reservationService.createReservation(dto, roomFacility1.getId(),
            visitorMember.getEmail());

        ReservationReadDto readDto = reservationService.findReservation(reservationId,
            visitorMember.getEmail());

        assertAll(
            () -> assertThat(readDto.getId()).isEqualTo(reservationId),
            () -> assertThat(readDto.getFacility().getId()).isEqualTo(roomFacility1.getId()),
            () -> assertThat(readDto.getMember().getId()).isEqualTo(visitorMember.getId()),
            () -> assertThat(readDto.getStatus()).isEqualTo(ReservationStatus.WAITING)
        );
    }

    @DisplayName("기존 예약과 시간이 겹친다면 Room은 예약할 수 없다.")
    @Test
    public void createReservationRoom_throwException_ifOverlappingReservation() {
        LocalDateTime reservationStart = now.toLocalDate().atTime(LocalTime.of(12, 0, 0));
        LocalDateTime reservationEnd = now.toLocalDate().atTime(LocalTime.of(14, 59, 59));
        ReservationCreateDto dto = new ReservationCreateDto(reservationStart, reservationEnd);
        reservationService.createReservation(dto, roomFacility1.getId(), visitorMember.getEmail());

        reservationStart = now.toLocalDate().atTime(LocalTime.of(13, 0, 0));
        reservationEnd = now.toLocalDate().atTime(LocalTime.of(15, 59, 59));
        ReservationCreateDto dto2 = new ReservationCreateDto(reservationStart, reservationEnd);
        assertThatThrownBy(() -> reservationService
            .createReservation(dto2, roomFacility1.getId(), visitorMember.getEmail()))
            .isInstanceOf(ConflictingReservationException.class);
    }

    @DisplayName("방문자가 본인의 예약을 취소할 수 있다.")
    @Test
    public void cancelReservation() {
        LocalDateTime reservationStart = now.toLocalDate().atTime(LocalTime.of(12, 0, 0));
        LocalDateTime reservationEnd = now.toLocalDate().atTime(LocalTime.of(14, 59, 59));
        ReservationCreateDto dto = new ReservationCreateDto(reservationStart, reservationEnd);
        Long reservationId = reservationService
            .createReservation(dto, roomFacility1.getId(), visitorMember.getEmail());

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
        LocalDateTime reservationStart = now.toLocalDate().atTime(LocalTime.of(12, 0, 0));
        LocalDateTime reservationEnd = now.toLocalDate().atTime(LocalTime.of(14, 59, 59));
        ReservationCreateDto dto = new ReservationCreateDto(reservationStart, reservationEnd);
        Long reservationId = reservationService
            .createReservation(dto, roomFacility1.getId(), visitorMember.getEmail());

        assertAll(
            () -> assertThatThrownBy(
                () -> reservationService.cancelReservation(reservationId, hostMember.getEmail()))
                .isInstanceOf(PermissionDeniedException.class)
        );
    }
}
