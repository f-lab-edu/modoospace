package com.modoospace.space.sevice;

import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.facility.FacilityCreateDto;
import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleCreateUpdateDto;
import com.modoospace.space.controller.dto.facilitySchedule.FacilityScheduleReadDto;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
import com.modoospace.space.controller.dto.timeSetting.TimeSettingCreateDto;
import com.modoospace.space.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@DataJpaTest
public class FacilityScheduleServiceTest {

    private FacilityScheduleService facilityScheduleService;

    @Autowired
    private FacilityScheduleRepository facilityScheduleRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    private Member hostMember;

    private Space space;

    private Facility facility;

    private LocalDate nowDate;

    @BeforeEach
    public void setUp() {
        facilityScheduleService = new FacilityScheduleService(facilityScheduleRepository, facilityRepository, memberRepository);

        hostMember = Member.builder()
            .email("host@email")
            .name("host")
            .role(Role.HOST)
            .build();
        memberRepository.save(hostMember);

        Category category = Category.builder()
            .name("스터디 공간")
            .build();
        categoryRepository.save(category);
        SpaceCreateUpdateDto spaceCreateDto = SpaceCreateUpdateDto.builder()
            .name("공간이름")
            .description("설명")
            .build();
        space = spaceCreateDto.toEntity(category, hostMember);
        spaceRepository.save(space);


        FacilityCreateDto createDto = FacilityCreateDto.builder()
            .name("스터디룸1")
            .facilityType(FacilityType.ROOM)
            .description("1~4인실 입니다.")
            .reservationEnable(true)
            .timeSettings(Arrays.asList(new TimeSettingCreateDto(LocalTime.of(9, 0, 0), LocalTime.of(17, 59, 59))))
            .build();
        facility = createDto.toEntity(space);
        facilityRepository.save(facility);

        nowDate = LocalDate.now();
    }

    @DisplayName("시설 스케줄 데이터를 생성한다.")
    @Test
    public void createFacilitySchedule(){
        FacilityScheduleCreateUpdateDto createDto = new FacilityScheduleCreateUpdateDto(LocalDateTime.of(nowDate, LocalTime.of(19, 0, 0)),
            (LocalDateTime.of(nowDate, LocalTime.of(23, 59, 59))));

        facilityScheduleService.createFacilitySchedule(facility.getId(), createDto, hostMember.getEmail());

        List<FacilityScheduleReadDto> facilityScheduleReadDtos = facilityScheduleService.findFacilityScheduleByLocalDate(facility.getId(), nowDate);
        System.out.println(facilityScheduleReadDtos);
    }
}
