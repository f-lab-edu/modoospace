package com.modoospace.config.elacticsearch;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

import java.time.Duration;

@Configuration
public class RestClientConfig extends AbstractElasticsearchConfiguration {

    @Value("${spring.elasticsearch.host}")
    private String host;
    @Value("${spring.elasticsearch.port}")
    private int port;
    @Value("${spring.elasticsearch.username}")
    private String username;
    @Value("${spring.elasticsearch.password}")
    private String password;

    @Override
    public RestHighLevelClient elasticsearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(host + ":" + port)
                .withBasicAuth(username, password)
                .withConnectTimeout(Duration.ofSeconds(10))
                .build();

        return RestClients.create(clientConfiguration).rest();
    }
}
