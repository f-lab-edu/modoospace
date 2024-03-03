package com.modoospace.space.repository;

import com.modoospace.space.domain.SpaceIndex;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class SpaceIndexQueryRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<Long> findIdByQuery(String queryString) {
        BoolQueryBuilder queryBuilder = makeQuery(queryString);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryBuilder)
            .build();

        return elasticsearchOperations.search(searchQuery, SpaceIndex.class)
            .stream()
            .map(searchHit -> searchHit.getContent().getId())
            .collect(Collectors.toList());
    }

    /**
     * {"query": {"query_string": {"fields": ["name", "description", "categoryName", "address",]
     * ,"query": "*사당* AND *스터디룸*"}}}
     **/
    private BoolQueryBuilder makeQuery(String queryString) {
        String[] terms = queryString.split("\\s+");
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        for (String term : terms) {
            queryBuilder = queryBuilder.must(
                QueryBuilders.queryStringQuery("*" + term + "*")
                    .field("name").field("description").field("categoryName").field("address")
            );
        }
        return queryBuilder;
    }
}


