package com.example.jedis.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.jedis.component.RedisComponent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Jedis를 활용하여 Redis에 대한 CRUD 작업을 테스트
 * <p>
 * <a href="https://redis.io/docs/latest/develop/clients/jedis/">Jedis guide(Java)</a>
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class JedisGuideTest {

  private static final long TTL_SECONDS = 60 * 10;

  @Autowired
  RedisComponent redisComponent;

  @Autowired
  ObjectMapper objectMapper;

  @Order(1)
  @DisplayName("문자열 저장 및 조회")
  @Test
  void testSetAndGetString() {

    // Given
    String redisKey = "string:1";
    String value = "Hello, Redis!";
    redisComponent.setString(redisKey, value, TTL_SECONDS);

    // When
    String retrievedValue = redisComponent.getString(redisKey);
    log.debug("testSetAndGetString value : {}", retrievedValue);

    // Then
    assertAll(
        () -> assertNotNull(retrievedValue),
        () -> assertEquals(value, retrievedValue)
    );
  }

  @Order(2)
  @DisplayName("단일 사용자 저장 및 조회")
  @Test
  void testSetAndGetJsonUser() {

    // Given
    String redisKey = "user:1";
    UserDto.User user1 = UserDto.User.of("Paul John", "paul.john@example.com", 42, "London");
    redisComponent.setJson(redisKey, user1, TTL_SECONDS);

    // When
    UserDto.User retrievedUser = redisComponent.getJsonObject(redisKey, UserDto.User.class);
    log.debug("testSetAndGetJsonObject user : {}", retrievedUser);

    // Then
    assertAll(
        () -> assertNotNull(retrievedUser),
        () -> assertEquals(user1.getName(), retrievedUser.getName()),
        () -> assertEquals(user1.getEmail(), retrievedUser.getEmail()),
        () -> assertEquals(user1.getAge(), retrievedUser.getAge()),
        () -> assertEquals(user1.getCity(), retrievedUser.getCity())
    );
  }

  @Order(3)
  @DisplayName("사용자 목록 저장 및 조회")
  @Test
  void testSetAndGetJsonUserList() throws Exception {

    // Given
    String redisKey = "user_list";
    List<UserDto.User> userList = Arrays.asList(
        UserDto.User.of("Paul John", "paul.john@example.com", 42, "London"),
        UserDto.User.of("Eden Zamir", "eden.zamir@example.com", 29, "Tel Aviv"),
        UserDto.User.of("Paul Zamir", "paul.zamir@example.com", 35, "Tel Aviv")
    );
    UserDto userDto = UserDto.builder().userList(userList).build();
    redisComponent.setJson(redisKey, userDto, TTL_SECONDS);

    // When
    UserDto retrievedUser = redisComponent.getJsonObject(redisKey, UserDto.class);
    log.debug("testSetAndGetJsonList userDto : {}",
        objectMapper.writeValueAsString(retrievedUser));

    // Then
    assertAll(
        () -> assertNotNull(retrievedUser),
        () -> assertNotNull(retrievedUser.getUserList()),
        () -> assertEquals(userList.size(), retrievedUser.getUserList().size())
    );
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class UserDto {

    private List<User> userList;

    @Getter
    @Builder
    @AllArgsConstructor(staticName = "of")
    @NoArgsConstructor
    public static class User {

      private String name;
      private String email;
      private int age;
      private String city;
    }
  }
}
