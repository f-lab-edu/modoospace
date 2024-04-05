package com.modoospace.config.elacticsearch;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

@Configuration
public class ElasticSearchConfig extends AbstractElasticsearchConfiguration {

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
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port));
        builder.setRequestConfigCallback(
                requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(10000) // 10초
        );
        builder.setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setMaxConnTotal(100) // 전체 최대 연결 수를 100개로 설정
                        .setMaxConnPerRoute(50) // 단일 라우트(호스트) 당 최대 연결 수
        );
        return new RestHighLevelClient(builder);
    }
}
