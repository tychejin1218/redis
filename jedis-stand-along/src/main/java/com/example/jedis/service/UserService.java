package com.example.jedis.service;

import com.example.jedis.dto.UserDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.aggr.Reducers;
import redis.clients.jedis.search.schemafields.NumericField;
import redis.clients.jedis.search.schemafields.TagField;
import redis.clients.jedis.search.schemafields.TextField;

/**
 * <a href="https://redis.io/docs/latest/develop/interact/search-and-query/indexing/">Redis Query
 * Engine > Indexing</a>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

  private final JedisPooled jedisPooled;

  /**
   * Redis에 객체를 JSON(ReJSON-RL) 형태로 저장
   *
   * @param key     Redis 키
   * @param userDto UserDto
   * @return UserDto
   */
  public UserDto saveUser(String key, UserDto userDto) {
    jedisPooled.jsonSetWithEscape(key, userDto);
    return jedisPooled.jsonGet(key, UserDto.class);
  }

  /**
   * 인덱스(indexName)를 사용하여 Redis에 저장된 객체를 조회
   *
   * @param indexName 검색할 인덱스 이름
   * @param query     검색에 사용할 쿼리
   * @return List&lt;Document&lt; 검색된 사용자 목록 (Document 객체의 리스트)
   */
  public List<Document> findUser(String indexName, Query query) {
    createIndex();
    return jedisPooled.ftSearch(indexName, query).getDocuments();
  }

  /**
   * 인덱스(indexName)를 사용하여 Redis에 저장된 객체의 특정 필드만 조회
   *
   * @param indexName 검색할 인덱스 이름
   * @param query     검색에 사용할 쿼리
   * @param field     반환할 필드 이름
   * @return List&lt;Document&lt; 검색된 사용자 목록에서 지정된 필드 값들 (Document 객체의 리스트)
   */
  public List<Document> findUserReturnField(String indexName, Query query, String field) {
    createIndex();
    return jedisPooled.ftSearch(indexName, query.returnFields(field)).getDocuments();
  }

  public AggregationResult findUserAggregate(String indexName, String query, String field) {
    createIndex();
    AggregationBuilder ab = new AggregationBuilder(query)
        .groupBy(field, Reducers.count().as("count"));
    return jedisPooled.ftAggregate(indexName, ab);
  }

  /**
   * JSON 형식의 데이터를 사용하는 인덱스를 생성
   */
  public void createIndex() {

    try {
      SearchResult searchResult = jedisPooled.ftSearch("idx:users");
      log.info("searchResult : {}", searchResult);
    } catch (Exception e) {
      jedisPooled.ftCreate("idx:users",
          FTCreateParams.createParams()
              .on(IndexDataType.JSON)
              .addPrefix("user:"),
          TextField.of("$.name").as("name"),
          TagField.of("$.city").as("city"),
          NumericField.of("$.age").as("age")
      );
      log.error("createIndex : {}", e);
    }
  }
}
