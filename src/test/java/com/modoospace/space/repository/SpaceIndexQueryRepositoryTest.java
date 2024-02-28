package com.modoospace.space.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.modoospace.JpaTestConfig;
import com.modoospace.SpaceIndexTestRepository;
import com.modoospace.space.domain.SpaceIndex;
import java.io.IOException;
import java.util.List;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(JpaTestConfig.class)
@ActiveProfiles("test")
class SpaceIndexQueryRepositoryTest {

    @Autowired
    private SpaceIndexQueryRepository spaceIndexQueryRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private SpaceIndexTestRepository spaceIndexTestRepository;

    @BeforeEach
    public void setUp() throws IOException, InterruptedException {
        spaceIndexTestRepository = new SpaceIndexTestRepository(restHighLevelClient);
        SpaceIndex spaceIndex1 = SpaceIndex.builder()
            .id(1L)
            .name("사당 스터디룸")
            .description("사당역 3번출구 5분거리에요.")
            .categoryName("스터디룸")
            .address("서울 관악구 남현동")
            .build();
        spaceIndexTestRepository.save(spaceIndex1);

        SpaceIndex spaceIndex2 = SpaceIndex.builder()
            .id(2L)
            .name("강남 스터디룸")
            .description("강남역 1번출구 5분거리에요.")
            .categoryName("스터디룸")
            .address("서울 서초구 서초동")
            .build();
        spaceIndexTestRepository.save(spaceIndex2);

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
    public void clear() throws IOException {
        spaceIndexTestRepository.deleteAll();
    }
}
