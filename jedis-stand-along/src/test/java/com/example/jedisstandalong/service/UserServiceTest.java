package com.example.jedisstandalong.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.example.jedisstandalong.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.aggr.AggregationResult;

@Slf4j
@SpringBootTest
class UserServiceTest {

  @Autowired
  private UserService userService;

  @Autowired
  private ObjectMapper objectMapper;

  @DisplayName("saveUser_Redis JSON 저장")
  @Test
  void testSaveUser() throws Exception {

    // Given
    String key01 = "user:1";
    UserDto user01 = UserDto.builder()
        .name("Paul John")
        .email("paul.john@example.com")
        .age(42)
        .city("London")
        .build();

    String key02 = "user:2";
    UserDto user02 = UserDto.builder()
        .name("Eden Zamir")
        .email("eden.zamir@example.com")
        .age(29)
        .city("Tel Aviv")
        .build();

    String key03 = "user:3";
    UserDto user03 = UserDto.builder()
        .name("Paul Zamir")
        .email("paul.zamir@example.com")
        .age(35)
        .city("Tel Aviv")
        .build();

    // When
    UserDto savedUser01 = userService.saveUser(key01, user01);
    UserDto savedUser02 = userService.saveUser(key02, user02);
    UserDto savedUser03 = userService.saveUser(key03, user03);

    // Then
    log.debug("savedUser01: {}", objectMapper.writeValueAsString(savedUser01));
    log.debug("savedUser02: {}", objectMapper.writeValueAsString(savedUser02));
    log.debug("savedUser03: {}", objectMapper.writeValueAsString(savedUser03));

    assertAll(
        () -> assertFalse(ObjectUtils.isEmpty(savedUser01)),
        () -> assertFalse(ObjectUtils.isEmpty(savedUser02)),
        () -> assertFalse(ObjectUtils.isEmpty(savedUser03))
    );
  }

  @DisplayName("findUser_Redis JSON 조회 시 Document 객체를 반환")
  @Test
  void testFindUser() throws Exception {

    // Given
    String indexName = "idx:users";
    Query query = new Query("Paul @age:[30 40]");

    // When
    List<Document> documents = userService.findUser(indexName, query);

    // Then
    assertFalse(ObjectUtils.isEmpty(documents));
    for (Document document : documents) {
      log.debug("properties: {}", objectMapper.writeValueAsString(document.getProperties()));
    }
  }

  @DisplayName("findUserReturnField_Redis JSON 조회 시 특정 필드의 Document 객체를 반환")
  @Test
  void testFindUserReturnField() throws Exception {

    // Given
    String indexName = "idx:users";
    Query query = new Query("Paul @age:[30 40]");
    String field = "city";

    // When
    List<Document> documents = userService.findUserReturnField(indexName, query, field);

    // Then
    assertFalse(ObjectUtils.isEmpty(documents));
    for (Document document : documents) {
      log.debug("properties: {}", objectMapper.writeValueAsString(document.getProperties()));
    }
  }

  @DisplayName("findUserAggregate_Redis JSON 조회 시 특정 필드의 Document 객체를 반환")
  @Test
  void testFindUserAggregate() {

    // Given
    String indexName = "idx:users";
    String query = "*";
    String field = "@city";

    // When
    AggregationResult aggregationResult = userService.findUserAggregate(indexName, query, field);

    // Then
    log.debug("aggregationResult: {}", aggregationResult);
    assertFalse(ObjectUtils.isEmpty(aggregationResult));
    for (int index = 0; index < aggregationResult.getTotalResults(); index++) {
      log.debug("city: {}", aggregationResult.getRow(index).get("city"));
      log.debug("count: {}", aggregationResult.getRow(index).get("count"));
    }
  }
}
