package com.modoospace;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

@Configuration
@Profile("test")
public class RestClientTestConfig {

    @Bean
    public RestHighLevelClient elasticsearchClient() {
        RestClientBuilder builder;
        try (ElasticsearchContainer container = new ElasticsearchContainer(
            "docker.elastic.co/elasticsearch/elasticsearch:7.17.9")) {
            container.start();

            BasicCredentialsProvider credentialProvider = new BasicCredentialsProvider();
            credentialProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elasticsearch", "elasticsearch"));

            builder = RestClient.builder(
                    HttpHost.create(container.getHttpHostAddress()))
                .setHttpClientConfigCallback(
                    httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(
                        credentialProvider)
                );
        }

        return new RestHighLevelClient(builder);
    }
}
