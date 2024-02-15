package com.modoospace.space.repository;

import com.modoospace.space.domain.SpaceIndex;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class SpaceIndexQueryRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
    {"query": {"query_string":
     {"fields": ["name", "description", "categoryName", "address"]
     ,"query": "*사당* AND *스터디룸*"}
     }}
     **/
    public List<Long> findByQueryString(String queryString) {
        String[] terms = queryString.split("\\s+");
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        for (String term : terms) {
            queryBuilder = queryBuilder.must(
                QueryBuilders.queryStringQuery("*" + term + "*")
                    .field("name").field("description").field("categoryName").field("address")
            );
        }

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryBuilder)
            .build();

        return elasticsearchOperations.search(searchQuery, SpaceIndex.class)
            .stream()
            .map(searchHit -> searchHit.getContent().getId())
            .collect(Collectors.toList());
    }
}


