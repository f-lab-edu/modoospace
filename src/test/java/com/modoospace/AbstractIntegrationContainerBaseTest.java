package com.modoospace;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationContainerBaseTest {

    private static final GenericContainer MY_REDIS_CONTAINER;
    private static final ElasticsearchContainer MY_ELASTICSEARCH_CONTAINER;

    private static final RabbitMQContainer MY_RABBITMQ_CONTAINER;

    static {
        MY_REDIS_CONTAINER = new GenericContainer<>("redis:6")
                .withExposedPorts(6379);
        MY_REDIS_CONTAINER.start();
        System.setProperty("spring.redis.host", MY_REDIS_CONTAINER.getHost());
        System.setProperty("spring.redis.port", MY_REDIS_CONTAINER.getMappedPort(6379).toString());
        System.setProperty("spring.redis.password", "");

        MY_ELASTICSEARCH_CONTAINER = new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.9"));
        MY_ELASTICSEARCH_CONTAINER.start();
        System.setProperty("spring.elasticsearch.host", MY_ELASTICSEARCH_CONTAINER.getHost());
        System.setProperty("spring.elasticsearch.port", MY_ELASTICSEARCH_CONTAINER.getMappedPort(9200).toString());
        System.setProperty("spring.elasticsearch.username", "elasticsearch");
        System.setProperty("spring.elasticsearch.password", "elasticsearch");

        MY_RABBITMQ_CONTAINER = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3-management"));
        MY_RABBITMQ_CONTAINER.start();
        System.setProperty("spring.rabbitmq.host", MY_RABBITMQ_CONTAINER.getHost());
        System.setProperty("spring.rabbitmq.port", MY_RABBITMQ_CONTAINER.getMappedPort(5672).toString());
        System.setProperty("spring.rabbitmq.username", "guest");
        System.setProperty("spring.rabbitmq.password", "guest");
        System.setProperty("spring.rabbitmq.retry_count", "3");
        RabbitMQTestInitializer.initializeRabbitMQ(MY_RABBITMQ_CONTAINER);
    }
}
