package com.modoospace.data.controller;

import com.modoospace.AbstractIntegrationContainerBaseTest;
import com.modoospace.common.exception.EmptyResponseException;
import com.modoospace.data.controller.dto.AddressResponse;
import com.modoospace.data.controller.dto.SpaceResponse;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.space.controller.dto.space.SpaceDetailResponse;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class DataControllerTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private DataController dataController;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Member member;

    @BeforeEach
    public void setUp() {
        member = Member.builder()
                .name("허정화")
                .email("wjdghkwhdl@jr.naver.com")
                .role(Role.HOST)
                .build();
        memberRepository.save(member);
        Category category = new Category("스터디룸");
        categoryRepository.save(category);
    }

    @DisplayName("외부api로 받아온 공간데이터가 비어있을 경우 Exception을 던진다.")
    @Test
    public void getSpace_throwException_ifEmptyResponse() {
        assertThatThrownBy(() -> dataController.getSpace("1"))
                .isInstanceOf(EmptyResponseException.class);
    }

    @DisplayName("외부Api로 받아온 데이터를 반환한다.")
    @Test
    public void getSpace() throws IOException, InterruptedException {
        SpaceResponse space = dataController.getSpace("58861");
        assertThat(space.getSpaceInfo().getName()).isEqualTo("감성공간 아르떼부암 ");
    }

    @DisplayName("KakaoApi로 받아온 주소데이터가 비어있을 경우 Exception을 던진다.")
    @Test
    public void getAddress_throwException_ifEmptyResponse() {
        assertThatThrownBy(() -> dataController.getAddress("address"))
                .isInstanceOf(EmptyResponseException.class);
    }

    @DisplayName("KakaoApi로 받아온 데이터를 반환한다.")
    @Test
    public void getAddress() throws IOException, InterruptedException {
        AddressResponse address = dataController.getAddress("서울 종로구 자하문로 254");
        assertThat(address.getDocuments().get(0).getX()).isEqualTo("126.963964049851");
        assertThat(address.getDocuments().get(0).getY()).isEqualTo("37.5969625614596");
    }

    @DisplayName("외부Api로 받아온 데이터를 모두스페이스 엔티티로 변환 후 저장하여 반환한다.")
    @Test
    public void saveSpace_throwException_ifEmptyResponse() {
        assertThatThrownBy(() -> dataController.saveSpace("2", member.getEmail()))
                .isInstanceOf(EmptyResponseException.class);
    }

    @DisplayName("외부api로 받아온 공간데이터가 비어있을 경우 변환하지 않고 Exception을 던진다.")
    @Test
    public void saveSpace() throws IOException, InterruptedException {
        SpaceDetailResponse space = dataController.saveSpace("58861", member.getEmail());
        assertThat(space.getName()).isEqualTo("감성공간 아르떼부암 ");
        assertThat(space.getFacilities()).hasSize(1);
    }
}