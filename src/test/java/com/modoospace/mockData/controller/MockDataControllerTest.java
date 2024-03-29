package com.modoospace.mockData.controller;

import com.modoospace.AbstractIntegrationContainerBaseTest;
import com.modoospace.common.exception.EmptyResponseException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import com.modoospace.mockData.controller.dto.MockAddressResponse;
import com.modoospace.mockData.controller.dto.MockSpaceResponse;
import com.modoospace.space.domain.Category;
import com.modoospace.space.domain.CategoryRepository;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class MockDataControllerTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private MockDataController mockDataController;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    HttpSession httpSession;

    private Member member;

    @BeforeEach
    public void setUp() {
        member = Member.builder()
                .name("허정화")
                .email("wjdghkwhdl@jr.naver.com")
                .role(Role.HOST)
                .build();
        member = memberRepository.save(member);
        httpSession.setAttribute("member", member.getEmail());

        Category category = new Category("스터디룸");
        categoryRepository.save(category);
    }

    @DisplayName("외부api로 받아온 공간데이터가 비어있을 경우 Exception을 던진다.")
    @Test
    public void getSpace_throwException_ifEmptyResponse() {
        assertThatThrownBy(() -> mockDataController.getSpace("1"))
                .isInstanceOf(EmptyResponseException.class);
    }

    @DisplayName("외부Api로 받아온 데이터를 반환한다.")
    @Test
    public void getSpace() throws IOException, InterruptedException {
        MockSpaceResponse space = mockDataController.getSpace("58861").getBody();

        assertThat(space).isNotNull();
        assertThat(space.getSpaceInfo().getName()).isEqualTo("감성공간 아르떼부암 ");
    }

    @DisplayName("KakaoApi로 받아온 주소데이터가 비어있을 경우 Exception을 던진다.")
    @Test
    public void getAddress_throwException_ifEmptyResponse() {
        assertThatThrownBy(() -> mockDataController.getAddress("address"))
                .isInstanceOf(EmptyResponseException.class);
    }

    @DisplayName("KakaoApi로 받아온 데이터를 반환한다.")
    @Test
    public void getAddress() throws IOException, InterruptedException {
        MockAddressResponse address = mockDataController.getAddress("서울 종로구 자하문로 254").getBody();

        assertThat(address).isNotNull();
        assertThat(address.getDocuments().get(0).getX()).isEqualTo("126.963964049851");
        assertThat(address.getDocuments().get(0).getY()).isEqualTo("37.5969625614596");
    }

    @DisplayName("외부api로 받아온 공간데이터가 비어있을 경우 변환하지 않고 Exception을 던진다.")
    @Test
    public void saveSpace_throwException_ifEmptyResponse() {
        assertThatThrownBy(() -> mockDataController.saveSpace("2", member))
                .isInstanceOf(EmptyResponseException.class);
    }

    @DisplayName("외부Api로 받아온 데이터를 모두스페이스 엔티티로 변환 후 저장하여 반환한다.")
    @Test
    public void saveSpace() throws IOException, InterruptedException {
        URI location = mockDataController.saveSpace("58861", member).getHeaders().getLocation();
        assertThat(location).isNotNull();

        Optional<Space> space = spaceRepository.findById(extractId(location));
        assertThat(space).isPresent();
        assertThat(space.get().getName()).isEqualTo("감성공간 아르떼부암 ");
    }

    private Long extractId(URI location) {
        String path = location.getPath();
        String id = path.substring(path.lastIndexOf("/") + 1);
        return Long.parseLong(id);
    }
}