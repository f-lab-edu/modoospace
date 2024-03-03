package com.modoospace.space.repository;

import com.modoospace.AbstractIntegrationContainerBaseTest;
import com.modoospace.space.domain.SpaceIndex;
import com.modoospace.space.domain.SpaceIndexRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpaceIndexQueryRepositoryTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private SpaceIndexQueryRepository spaceIndexQueryRepository;

    @Autowired
    private SpaceIndexRepository spaceIndexRepository;

    @BeforeEach
    public void setUp() throws IOException, InterruptedException {
        SpaceIndex spaceIndex1 = SpaceIndex.builder()
                .id(1L)
                .name("사당 스터디룸")
                .description("사당역 3번출구 5분거리에요.")
                .categoryName("스터디룸")
                .address("서울 관악구 남현동")
                .build();
        spaceIndexRepository.save(spaceIndex1);

        SpaceIndex spaceIndex2 = SpaceIndex.builder()
                .id(2L)
                .name("강남 스터디룸")
                .description("강남역 1번출구 5분거리에요.")
                .categoryName("스터디룸")
                .address("서울 서초구 서초동")
                .build();
        spaceIndexRepository.save(spaceIndex2);
    }

    @Test
    public void findIdByQuery_By사당() {
        List<Long> retId = spaceIndexQueryRepository.findIdByQuery("사당");
        assertThat(retId).hasSize(1);
    }

    @Test
    public void findIdByQuery_By스터디룸() {
        List<Long> retId = spaceIndexQueryRepository.findIdByQuery("스터디룸");
        assertThat(retId).hasSize(2);
    }

    @AfterEach
    public void clear() {
        spaceIndexRepository.deleteAll();
    }
}
