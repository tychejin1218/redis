# Jedis를 활용하여 Redis에 대한 CRUD 작업을 테스트

Jedis를 활용하여 Redis에 대한 CRUD 작업을 수행하는 방법을 설명하겠습니다.

## 1. Docker를 활용하여 Redis 환경 설정

Docker를 사용하여 Redis 서버를 설치 및 실행합니다.

```bash
docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
```

- 포트 6379: Redis 서버와의 기본 연결
- 포트 8001: RedisInsight와 같은 GUI 도구와의 연결

[Docker Hub- redis/redis-stack](https://hub.docker.com/r/redis/redis-stack)

## 2. 의존성 추가

`build.gradle` 파일에 Redis 및 Jedis와의 연동에 필요한 의존성을 추가합니다.

**build.gradle**

```groovy
dependencies {
    // Redis
    implementation('org.springframework.boot:spring-boot-starter-data-redis') {
        exclude group: 'io.lettuce', module: 'lettuce-core'
    }

    // Jedis
    implementation 'redis.clients:jedis:5.1.0'
}
```

[Maven Repository- Jedis](https://mvnrepository.com/artifact/redis.clients/jedis/5.1.0)

## 3. RedisConfig 설정

Redis 서버와의 연동을 위해 `JedisPooled` 객체를 빈으로 등록합니다.

**RedisConfig.java**

```java
package com.example.jedis.config;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisConfig {

  @Value("${redis.stand-alone.host}")
  private String standAloneHost;

  @Value("${redis.stand-alone.port}")
  private String standAlonePort;

  /**
   * RedisConnectionFactory 빈을 생성
   *
   * @return RedisConnectionFactory 빈
   */
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new JedisConnectionFactory(
        new RedisStandaloneConfiguration(standAloneHost, Integer.parseInt(standAlonePort)));
  }

  /**
   * RedisConnectionFactory를 사용하여 JedisPooled 빈을 생성
   *
   * @param redisConnectionFactory RedisConnectionFactory
   * @return JedisPooled 빈
   */
  @Bean
  public JedisPooled jedisPooled(RedisConnectionFactory redisConnectionFactory) {
    JedisConnectionFactory jedisConnectionFactory =
        (JedisConnectionFactory) redisConnectionFactory;
    return new JedisPooled(
        Objects.requireNonNull(jedisConnectionFactory.getPoolConfig()),
        jedisConnectionFactory.getHostName(),
        jedisConnectionFactory.getPort());
  }
}
```

## 4. RedisComponent 구현

`RedisComponent` 클래스는 Jedis를 활용해 데이터를 저장하고, 조회하는 기능을 제공합니다.

### RedisComponent에서 사용한 Jedis 메서드

- **set(String key, String value)**: 주어진 키에 문자열 값을 저장합니다. 기존 값이 있다면 덮어씁니다.
- **expire(String key, long seconds)**: 특정 키에 대한 만료 시간을 설정합니다. 초 단위로 설정되며, 해당 시간이 지나면 키와 값이 삭제됩니다.
- **get(String key)**: 특정 키와 연관된 값을 조회합니다. 키가 존재하지 않으면 `null`을 반환합니다.
- **jsonSetWithEscape(String key, Object t)**: 객체를 JSON 형식으로 변환하고, 지정된 키에 저장합니다.
- **jsonGet(String key)**: JSON 형태로 저장된 데이터를 객체로 변환하여 가져옵니다. 저장된 데이터가 없는 경우 `null`을 반환합니다.

### 참고

- [Jedis GitHub Repository](https://github.com/redis/jedis)
- [Jedis JavaDocs (공식 API 문서)](https://javadoc.io/doc/redis.clients/jedis/latest/index.html)
- [Redis 공식 사이트 (Redis Commands)](https://redis.io/docs/latest/commands/)

### RedisComponent의 메서드

`RedisComponent`는 Jedis 메서드들을 활용하여 Redis 서버와의 연동을 위해 필요한 메서드를 제공합니다.

- **`setString(String key, String value, long ttl)`**: `set` 메서드를 사용하여 문자열 데이터를 Redis에 저장하고,
  `expire` 메서드를 이용해 키의 만료 시간을 설정합니다.
- **`getString(String key)`**: `get` 메서드를 사용하여 Redis에 저장된 문자열 값을 조회합니다. 키가 존재하지 않으면 `null`을 반환합니다.
- **`setJson(String key, T t, long ttl)`**: `jsonSetWithEscape`를 사용하여 객체를 JSON으로 변환한 후 저장하고,
  `expire`를 활용하여 키의 만료 시간을 설정합니다.
- **`getJsonObject(String key, Class<T> clazz)`**: `jsonGet`을 사용해 특정 키의 데이터를 가져오고, 지정된 클래스 타입(
  `clazz`)으로 변환하여 반환합니다.

**RedisComponent.java**

```java
package com.example.jedis.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.json.Path2;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisComponent {

  private final JedisPooled jedisPooled;
  private final ObjectMapper objectMapper;

  /**
   * 문자열을 저장하고, 지정된 TTL(만료 시간)을 설정
   *
   * @param key   저장할 키
   * @param value 저장할 문자열
   * @param ttl   만료 시간(초 단위)
   */
  public void setString(String key, String value, long ttl) {
    jedisPooled.set(key, value);
    jedisPooled.expire(key, ttl);
  }

  /**
   * 키를 사용하여 저장된 문자열을 조회
   *
   * @param key 검색할 키
   * @return 저장된 문자열을 반환. 키가 존재하지 않으면 null을 반환
   */
  public String getString(String key) {
    return jedisPooled.get(key);
  }

  /**
   * 객체를 JSON 형식으로 저장하고, 지정된 TTL(만료 시간)을 설정
   *
   * @param key 저장할 키
   * @param t   저장할 객체
   * @param ttl 만료 시간(초 단위)
   * @param <T> 저장할 객체의 유형
   */
  public <T> void setJson(String key, T t, long ttl) {
    jedisPooled.jsonSetWithEscape(key, t);
    jedisPooled.expire(key, ttl);
  }

  /**
   * 키를 사용하여 저장된 객체를 조회
   *
   * @param key   검색할 키
   * @param clazz 검색할 객체의 클래스 유형
   * @param <T>   검색할 객체의 유형
   * @return 저장된 객체를 반환. JSON 파싱 실패 시 null을 반환
   */
  public <T> T getJsonObject(String key, Class<T> clazz) {

    try {
      Object object = jedisPooled.jsonGet(key);

      if (object != null && !ObjectUtils.isEmpty(object.toString())) {
        String jsonString = objectMapper.writeValueAsString(object);
        return objectMapper.readValue(jsonString, clazz);
      }
    } catch (JsonProcessingException e) {
      log.error("getJson key : {}", key, e);
    }

    return null;
  }
}
```

## 5. 테스트 클래스 구현

JUnit을 사용하여 `RedisComponent`의 기능을 테스트하는 코드를 작성합니다. 이 코드는 문자열 저장 및 조회, 단일 사용자 저장 및 조회, 사용자 목록 저장 및
조회를 다룹니다.

### 메서드 설명

1. **`testSetAndGetString()`**

- Redis에 문자열 데이터를 저장하고(`setString`), 저장된 데이터를 다시 조회(`getString`)하여 유효성을 테스트합니다.

2. **`testSetAndGetJsonUser()`**

- 단일 사용자 객체를 Redis에 JSON 형식으로 저장(`setJson`)하고, 이를 객체(`getJsonObject`)로 다시 조회하여 유효성을 확인합니다.

3. **`testSetAndGetJsonUserList()`**

- 사용자 객체들의 리스트를 Redis에 JSON 형식으로 저장(`setJson`)하고, 이를 리스트 형태의 객체로 다시 조회하여 유효성을 테스트합니다.

**JedisGuideTest.java**

```java
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
```

---

# Jedis를 활용하여 Redis에 JSON Path 기능을 테스트

Jedis 라이브러리를 사용하여 Redis에 JSON Path 기능을 활용하는 방법을 설명하겠습니다.

## 1. Docker를 활용하여 Redis 환경 설정

Docker를 활용하여 Redis 서버를 설치하고 실행합니다.

```bash
docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
```

- 포트 6379: Redis 서버와의 기본 연결
- 포트 8001: RedisInsight와 같은 GUI 도구와의 연결

[Docker Hub- redis/redis-stack](https://hub.docker.com/r/redis/redis-stack)

## 2. 의존성 추가

`build.gradle` 파일에 Redis 및 Jedis와의 연동에 필요한 의존성을 추가합니다.

**build.gradle**

```groovy
dependencies {
    // Redis
    implementation('org.springframework.boot:spring-boot-starter-data-redis') {
        exclude group: 'io.lettuce', module: 'lettuce-core'
    }

    // Jedis
    implementation 'redis.clients:jedis:5.1.0'
}
```

[Maven Repository- Jedis](https://mvnrepository.com/artifact/redis.clients/jedis/5.1.0)

## 3. RedisConfig 설정

Redis 서버와의 연동을 위해 `JedisPooled` 객체를 빈으로 등록합니다.

**RedisConfig.java**

```java
package com.example.jedis.config;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisConfig {

  @Value("${redis.stand-alone.host}")
  private String standAloneHost;

  @Value("${redis.stand-alone.port}")
  private String standAlonePort;

  /**
   * RedisConnectionFactory 빈을 생성
   *
   * @return RedisConnectionFactory 빈
   */
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new JedisConnectionFactory(
        new RedisStandaloneConfiguration(standAloneHost, Integer.parseInt(standAlonePort)));
  }

  /**
   * RedisConnectionFactory를 사용하여 JedisPooled 빈을 생성
   *
   * @param redisConnectionFactory RedisConnectionFactory
   * @return JedisPooled 빈
   */
  @Bean
  public JedisPooled jedisPooled(RedisConnectionFactory redisConnectionFactory) {
    JedisConnectionFactory jedisConnectionFactory =
        (JedisConnectionFactory) redisConnectionFactory;
    return new JedisPooled(
        Objects.requireNonNull(jedisConnectionFactory.getPoolConfig()),
        jedisConnectionFactory.getHostName(),
        jedisConnectionFactory.getPort());
  }
}
```

## 4. RedisComponent 구현

`RedisComponent` 클래스는 Jedis를 활용해 객체의 JSON 변환 및 저장, 조회와 같은 기능을 제공합니다.

### RedisComponent에서 사용한 Jedis 메서드

- **expire(String key, long seconds)**: 특정 키의 만료 시간을 설정합니다. 초 단위로 설정되며, 해당 시간이 지나면 키와 값이 삭제됩니다.
- **jsonSetWithEscape(String key, Object t)**: 객체를 JSON 형식으로 변환하고 지정된 키에 저장합니다.
- **jsonGet(String key)**: JSON 형태로 저장된 데이터를 객체로 변환하여 가져옵니다. 저장된 데이터가 없는 경우 `null`을 반환합니다.
- **jsonGet(String key, String path)**: 지정된 키와 경로에 해당하는 JSON 데이터를 객체로 변환하여 가져옵니다. 경로나 데이터가 유효하지 않으면
  `null`을 반환합니다.

### 참고

- [Jedis GitHub Repository](https://github.com/redis/jedis)
- [Jedis JavaDocs (공식 API 문서)](https://javadoc.io/doc/redis.clients/jedis/latest/index.html)
- [Redis 공식 사이트 (Redis Commands)](https://redis.io/docs/latest/commands/)

### RedisComponent의 메서드

`RedisComponent`는 Jedis 메서드들을 활용하여 Redis 서버와의 연동을 위해 필요한 메서드를 제공합니다.

- **`setJson(String key, T t, long ttl)`**: `jsonSetWithEscape`를 사용하여 객체를 JSON으로 변환한 후 저장하고,
  `expire`를 활용하여 키의 만료 시간을 설정합니다.
- **`getJsonArray(String key, String path)`**: `jsonGet` 메서드 호출 시 경로(`path`)를 추가하여 지정된 경로의 JSON 배열을
  조회합니다. 데이터가 없는 경우 `null`을 반환합니다.
- **`getJsonObject(String key, Class<T> clazz)`**: `jsonGet`을 사용해 특정 키의 데이터를 가져오고, 지정된 클래스 타입(
  `clazz`)으로 변환하여 반환합니다.
- **`getJsonList(String key, Class<T> clazz, String path)`**: `jsonGet`을 활용하여 경로(`path`)에 해당하는 데이터를
  조회하고, 이를 리스트 형태로 변환하여 반환합니다.

**RedisComponent.java**

```java
package com.example.jedis.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.json.Path2;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisComponent {

  private final JedisPooled jedisPooled;
  private final ObjectMapper objectMapper;

  /**
   * 객체를 JSON 형식으로 저장하고, 지정된 TTL(만료 시간)을 설정
   *
   * @param key 저장할 키
   * @param t   저장할 객체
   * @param ttl 만료 시간(초 단위)
   * @param <T> 저장할 객체의 유형
   */
  public <T> void setJson(String key, T t, long ttl) {
    jedisPooled.jsonSetWithEscape(key, t);
    jedisPooled.expire(key, ttl);
  }

  /**
   * 키와 경로를 사용하여 JSONArray를 조회
   *
   * @param key  검색할 키
   * @param path JSONPath 표현식으로, JSON 내에서 데이터를 검색할 경로
   * @return 키와 경로에 해당하는 JSON 배열을 반환. 키 또는 경로가 유효하지 않으면 null을 반환
   */
  public JSONArray getJsonArray(String key, String path) {
    return (JSONArray) jedisPooled.jsonGet(key, Path2.of(path));
  }

  /**
   * 키를 사용하여 저장된 객체를 조회
   *
   * @param key   검색할 키
   * @param clazz 검색할 객체의 클래스 유형
   * @param <T>   검색할 객체의 유형
   * @return 저장된 객체를 반환. JSON 파싱 실패 시 null을 반환
   */
  public <T> T getJsonObject(String key, Class<T> clazz) {

    try {
      Object object = jedisPooled.jsonGet(key);

      if (object != null && !ObjectUtils.isEmpty(object.toString())) {
        String jsonString = objectMapper.writeValueAsString(object);
        return objectMapper.readValue(jsonString, clazz);
      }
    } catch (JsonProcessingException e) {
      log.error("getJson key : {}", key, e);
    }

    return null;
  }

  /**
   * 키와 경로를 사용하여 저장된 리스트를 조회
   *
   * @param key   검색할 키
   * @param clazz 검색할 객체의 클래스 유형
   * @param path  JSONPath 표현식으로, JSON 내에서 데이터를 검색할 경로
   * @param <T>   검색할 객체의 유형
   * @return 저장된 리스트를 반환. JSON 파싱 실패 시 빈 리스트를 반환
   */
  public <T> List<T> getJsonList(String key, Class<T> clazz, String path) {

    try {
      Object object = jedisPooled.jsonGet(key, Path2.of(path));

      if (object != null && !ObjectUtils.isEmpty(object.toString())) {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        return objectMapper.readValue(object.toString(),
            typeFactory.constructCollectionType(List.class, clazz));
      }

    } catch (JsonProcessingException e) {
      log.error("getJsonArray key : {}", key, e);
    }

    return Collections.emptyList();
  }
}
```

## 5. 테스트 클래스 구현

`JsonDataTypeTest` 클래스를 통해 Redis에 JSON Path 기능을 테스트합니다.

### 메서드 설명

1. **`testSetAndGetJsonObject()`**

- Redis에 객체 데이터를 JSON 형식으로 저장(`setJson`)하고, 이를 객체(`getJsonObject`)로 조회하여 유효성을 확인합니다.

2. **`testSetAndGetJsonArray()`**

- Redis에서 특정 JSON Path 경로를 사용하여 `JSONArray` 데이터를 조회(`getJsonArray`)합니다. 여러 JSON Path를 통해 데이터를 확인하며,
  유효성을 테스트합니다.

3. **`testSetAndGetJsonList()`**

- Redis에서 특정 JSON Path 경로를 통해 리스트 형태의 데이터를 조회(`getJsonList`)하여 유효성을 확인합니다.

**JsonDataTypeTest.java**

```java
package com.example.jedis.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.jedis.component.RedisComponent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Jedis를 활용하여 Redis에 JSON Path 기능을 테스트
 * <p>
 * <a
 * href="https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/json-document-overview.html">Json
 * data type overview</a>
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class JsonDataTypeTest {

  private static final String REDIS_KEY = "k1";
  private static final long TTL_SECONDS = 60 * 10;

  @Autowired
  RedisComponent redisComponent;

  @Autowired
  ObjectMapper objectMapper;

  @Order(1)
  @DisplayName("객체를 JSON 형식으로 저장하고, 키를 사용하여 객체를 조회")
  @Test
  void testSetAndGetJsonObject() throws Exception {

    // Given
    StoreDto storeDto = createStoreDto();
    redisComponent.setJson(REDIS_KEY, storeDto, TTL_SECONDS);

    // When
    StoreDto retrievedStore = redisComponent.getJsonObject(REDIS_KEY, StoreDto.class);
    log.debug("testSetAndGetJsonObject retrievedStore : {}",
        objectMapper.writeValueAsString(retrievedStore));

    // Then
    assertAll(
        () -> assertNotNull(retrievedStore),
        () -> assertFalse(retrievedStore.getStore().getBook().isEmpty()),
        () -> assertNotNull(retrievedStore.getStore().getBicycle())
    );
  }

  @Order(2)
  @DisplayName("키와 경로를 사용하여 JsonArray를 조회")
  @Test
  void testSetAndGetJsonArray() {

    // Given
    StoreDto storeDto = createStoreDto();
    redisComponent.setJson(REDIS_KEY, storeDto, TTL_SECONDS);

    // When & Then
    for (String jsonPath : initJsonPaths()) {

      JSONArray jsonArray = redisComponent.getJsonArray(REDIS_KEY, jsonPath);
      log.debug("testSetAndGetJsonArray jsonPath {} : jsonArray : {}",
          jsonPath, jsonArray.toString());

      assertNotNull(jsonArray);
    }
  }

  @Order(3)
  @DisplayName("키와 경로를 사용하여 JsonList를 조회")
  @Test
  void testSetAndGetJsonList() throws Exception {

    // Given
    StoreDto storeDto = createStoreDto();
    redisComponent.setJson(REDIS_KEY, storeDto, TTL_SECONDS);

    // When
    List<StoreDto> storeList = redisComponent.getJsonList(REDIS_KEY, StoreDto.class, "$");
    log.debug("storeList : {}", objectMapper.writeValueAsString(storeList));

    // Then
    assertNotNull(storeList);
  }

  private StoreDto createStoreDto() {

    List<StoreDto.Store.Book> books = List.of(
        StoreDto.Store.Book.of("reference", "Nigel Rees", "Sayings of the Century",
            null, 8.95, true, true),
        StoreDto.Store.Book.of("fiction", "Evelyn Waugh", "Sword of Honour",
            null, 12.99, false, true),
        StoreDto.Store.Book.of("fiction", "Herman Melville", "Moby Dick",
            "0-553-21311-3", 8.99, true, false),
        StoreDto.Store.Book.of("fiction", "J. R. R. Tolkien", "The Lord of the Rings",
            "0-395-19395-8", 22.99, false, false)
    );

    StoreDto.Store.Bicycle bicycle = StoreDto.Store.Bicycle.of("red", 19.95, true, false);
    return StoreDto.of(StoreDto.Store.of(books, bicycle));
  }

  private String[] initJsonPaths() {
    return new String[]{
        "$.store.book[*].author",         // store의 각 book의 author
        "$..author",                      // JSON의 전체 author
        "$.store.*",                      // store 객체의 전체 속성
        "$[\"store\"].*",                 // store 객체의 전체 속성 (다른 접근 방법)
        "$.store..price",                 // store 내 전체 속성의 price
        "$..*",                           // JSON 구조의 전체 요소
        "$..book[*]",                     // 각각의 book 객체
        "$..book[0]",                     // 첫 번째 book
        "$..book[-1]",                    // 마지막 book
        "$..book[0:2]",                   // 처음 두 권의 book
        "$..book[0,1]",                   // 첫 번째와 두 번째 book
        "$..book[0:4]",                   // 인덱스 0부터 3까지의 book
        "$..book[0:4:2]",                 // 인덱스 0과 2의 book
        "$..book[?(@.isbn)]",             // ISBN 있는 각각의 book
        "$..book[?(@.price<10)]",         // 가격이 $10 미만인 각각의 book
        "$..book[?(@[\"price\"] < 10)]",  // 가격이 $10 미만인 각각의 book (다른 접근 방법)
        "$..book[?(@.price>=10&&@.price<=100)]",  // 가격이 $10 이상 $100 이하인 각각의 book
        "$..book[?(@.sold==true||@.in-stock==false)]",  // sold가 true이거나 in-stock이 false인 각각의 book
        "$.store.book[?(@.[\"category\"] == \"fiction\")]", // fiction 카테고리의 각 book
        "$.store.book[?(@.[\"category\"] != \"fiction\")]"  // fiction 카테고리에 속하지 않는 각각의 book
    };
  }

  @Getter
  @Builder
  @AllArgsConstructor(staticName = "of")
  @NoArgsConstructor
  public static class StoreDto {

    private Store store;

    @Getter
    @Builder
    @AllArgsConstructor(staticName = "of")
    @NoArgsConstructor
    public static class Store {

      private List<Book> book;
      private Bicycle bicycle;

      @Getter
      @Builder
      @AllArgsConstructor(staticName = "of")
      @NoArgsConstructor
      public static class Book {

        private String category;
        private String author;
        private String title;
        private String isbn;
        private Double price;
        private Boolean inStock;
        private Boolean sold;
      }

      @Getter
      @Builder
      @AllArgsConstructor(staticName = "of")
      @NoArgsConstructor
      public static class Bicycle {

        private String color;
        private Double price;
        private Boolean inStock;
        private Boolean sold;
      }
    }
  }
}
```
