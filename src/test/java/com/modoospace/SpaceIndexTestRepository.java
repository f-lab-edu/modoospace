package com.modoospace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modoospace.space.domain.SpaceIndex;
import java.io.IOException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;

public class SpaceIndexTestRepository {

    private final RestHighLevelClient restHighLevelClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SpaceIndexTestRepository(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    public void save(SpaceIndex spaceIndex) throws IOException, InterruptedException {
        String jsonString = objectMapper.writeValueAsString(spaceIndex);
        IndexRequest indexRequest = new IndexRequest("space")
            .id(spaceIndex.getId().toString())
            .source(jsonString, XContentType.JSON);
        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        Thread.sleep(1000); // index를 저장하는데 텀이 필요함.
    }

    public void deleteAll() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("space");
        restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
    }
}
