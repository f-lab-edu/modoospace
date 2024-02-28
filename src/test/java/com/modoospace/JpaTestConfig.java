package com.modoospace;

import com.modoospace.alarm.repository.AlarmQueryRepository;
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.space.repository.FacilityQueryRepository;
import com.modoospace.space.repository.ScheduleQueryRepository;
import com.modoospace.space.repository.SpaceIndexQueryRepository;
import com.modoospace.space.repository.SpaceQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

@TestConfiguration
public class JpaTestConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    public RestHighLevelClient elasticsearchClient() {
        ElasticsearchContainer container
            = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.17.9");
        container.start();

        BasicCredentialsProvider credentialProvider = new BasicCredentialsProvider();
        credentialProvider.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials("elasticsearch", "elasticsearch"));

        RestClientBuilder builder = RestClient.builder(
                HttpHost.create(container.getHttpHostAddress()))
            .setHttpClientConfigCallback(
                httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(
                    credentialProvider)
            );

        return new RestHighLevelClient(builder);
    }

    @Bean
    public ElasticsearchOperations elasticsearchOperations() {
        return new ElasticsearchRestTemplate(elasticsearchClient());
    }

    @Bean
    public ReservationQueryRepository reservationQueryRepository() {
        return new ReservationQueryRepository(jpaQueryFactory());
    }

    @Bean
    public SpaceQueryRepository spaceQueryRepository() {
        return new SpaceQueryRepository(jpaQueryFactory(), spaceIndexQueryRepository());
    }

    @Bean
    public FacilityQueryRepository facilityQueryRepository() {
        return new FacilityQueryRepository(jpaQueryFactory());
    }

    @Bean
    public ScheduleQueryRepository facilityScheduleQueryRepository() {
        return new ScheduleQueryRepository(jpaQueryFactory());
    }

    @Bean
    public AlarmQueryRepository alarmQueryRepository() {
        return new AlarmQueryRepository(jpaQueryFactory());
    }

    @Bean
    public SpaceIndexQueryRepository spaceIndexQueryRepository() {
        return new SpaceIndexQueryRepository(elasticsearchOperations());
    }
}
