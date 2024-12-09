# StringRedisTemplate과 RedisTemplate을 이용한 Redis 활용

Spring Framework에서는 Redis 서버와의 연동을 돕기 위해 두 가지 주요 템플릿 클래스를 제공합니다: `RedisTemplate`과
`StringRedisTemplate`. 각 클래스의 특징과 사용 사례는 다음과 같습니다.

### `RedisTemplate`

- **일반적 용도**:
    - `RedisTemplate`은 다양한 Redis 데이터 타입과 연동할 수 있는 일반적인 템플릿 클래스입니다.
- **유연한 직렬화**:
    - 다양한 데이터 타입을 지원하며, 사용자 정의 직렬화 및 역직렬화 방식을 설정할 수 있습니다.
- **사용 사례**:
    - 복잡한 데이터 구조나 객체를 Redis에 저장하거나 검색할 때 적합합니다.

### `StringRedisTemplate`

- **특화된 문자 처리**:
    - `StringRedisTemplate`은 `RedisTemplate`을 확장하여 문자열 데이터를 기본적으로 처리합니다.
- **간편한 사용**:
    - 키와 값을 문자열로 간단하게 설정하고 사용할 수 있어, 문자열 데이터의 저장과 조회에 용이합니다.
- **사용 사례**:
    - 간단한 문자열 데이터의 저장 및 조회를 요구하는 애플리케이션에서 주로 활용됩니다.

이 두 템플릿은 Redis에 데이터를 저장, 조회, 삭제할 때 매우 유용하며, 데이터 타입 및 직렬화 요구 사항에 따라 적절한 템플릿을 선택하여 사용할 수 있습니다.

## Docker를 이용한 Redis Cluster 환경 구성

`grokzen/redis-cluster` Redis 클러스터 환경을 쉽게 설정할 수 있는 Docker 이미지입니다. 아래는 다음은 `docker-compose.yml` 파일의
예시입니다.

### docker-compose.xml

```yaml
version: '3'
services:
  redis-cluster:
    container_name: redis-cluster-6
    image: grokzen/redis-cluster:7.0.15
    environment:
      - IP=0.0.0.0
      - BIND_ADDRESS=0.0.0.0
      - INITIAL_PORT=7001
      - MASTERS=3
      - SLAVES_PER_MASTER=1
    ports:
      - "7001-7006:7001-7006"
```

- **환경 변수**:
    - `IP`와 `BIND_ADDRESS`: Redis 인스턴스가 특정 IP에 바인딩되게 설정합니다. `0.0.0.0`으로 설정하면 모든 네트워크 인터페이스에서 접근 가능하게
      만듭니다.
    - `INITIAL_PORT`: 클러스터의 시작 포트를 지정합니다.
    - `MASTERS`: 클러스터 내 마스터 노드의 개수를 설정합니다.
    - `SLAVES_PER_MASTER`: 각 마스터 노드당 슬레이브 노드의 개수를 설정합니다.

- **포트 매핑**:
    - `7001-7006:7001-7006`은 로컬의 포트 7001에서 7006까지를 컨테이너의 동일한 포트로 매핑하여 각 클러스터의 각 노드에 접근할 수 있습니다.

## Redis EndPoint 추가

Redis 클러스터에 연결할 때 필요한 클러스터의 엔드포인트(예: 노드 중 하나의 IP와 포트)를 `application.yml` 파일에 추가합니다.

```yaml
# Redis 설정
redis:
  cluster:
    nodes:
      127.0.0.1:7001
```

## RedisConfig 클래스

`RedisConfig` 클래스는 Redis 클러스터 연결을 위한 클라이언트 설정을 담당합니다. 여기서는 `RedisTemplate`과 `StringRedisTemplate`의
직렬화를 위해 `RedisSerializer`를 사용하였습니다.

- **`RedisSerializer.string()`**: 데이터를 문자열 형식으로 직렬화 및 역직렬화합니다. 주로 키 값의 직렬화에 사용되며, 이를 통해 데이터를 일관되게
  관리할 수 있습니다.
- **`RedisSerializer.json()`**: 데이터를 JSON 형식으로 직렬화 및 역직렬화합니다. 객체 데이터를 JSON 문자열로 변환하여 저장하고 객체로 쉽게 변환할
  수 있습니다.

```java
package com.example.redis.config;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${redis.cluster.nodes}")
  private List<String> nodes;

  /**
   * Redis 클러스터에 연결하기 위한 LettuceConnectionFactory 빈을 생성
   *
   * @return LettuceConnectionFactory 객체
   */
  @Bean
  public LettuceConnectionFactory lettuceConnectionFactory() {

    RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(nodes);

    ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
        .dynamicRefreshSources(true)
        // 클러스터 토폴로지를 60초마다 주기적으로 갱신하도록 설정
        .enablePeriodicRefresh(Duration.ofSeconds(60))
        // 연결 오류, 시간 초과 등 즉각적으로 클러스터 토플로지를 갱신하도록 설정
        .enableAllAdaptiveRefreshTriggers()
        // 갱신 타임아웃을 30초로 설정
        .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(30))
        .build();

    ClusterClientOptions clientOptions = ClusterClientOptions.builder()
        .autoReconnect(true)
        .topologyRefreshOptions(topologyRefreshOptions)
        .build();

    LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
        .clientOptions(clientOptions)
        // 읽기 작업을 우선적으로 수행하도록 설정
        .readFrom(ReadFrom.REPLICA_PREFERRED)
        .build();

    return new LettuceConnectionFactory(clusterConfig, clientConfig);
  }

  /**
   * 문자열을 저장하기 위한 StringRedisTemplate 빈을 생성
   *
   * @param lettuceConnectionFactory Lettuce를 통한 Redis 연결 팩토리
   * @return StringRedisTemplate 객체
   */
  @Bean
  public StringRedisTemplate stringRedisTemplate(
      LettuceConnectionFactory lettuceConnectionFactory) {
    return new StringRedisTemplate(lettuceConnectionFactory);
  }

  /**
   * 객체를 저장하기 위한 RedisTemplate 빈을 생성
   *
   * @param lettuceConnectionFactory Lettuce를 통한 Redis 연결 팩토리
   * @return RedisTemplate 객체
   */
  @Bean
  public RedisTemplate<String, Object> objectRedisTemplate(
      LettuceConnectionFactory lettuceConnectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(lettuceConnectionFactory);
    redisTemplate.setKeySerializer(RedisSerializer.string());
    redisTemplate.setHashKeySerializer(RedisSerializer.string());
    redisTemplate.setDefaultSerializer(RedisSerializer.json());
    redisTemplate.setValueSerializer(RedisSerializer.json());
    redisTemplate.setHashValueSerializer(RedisSerializer.json());
    return redisTemplate;
  }

  /**
   * 정수를 저장하기 위한 RedisTemplate 빈을 생성
   *
   * @param lettuceConnectionFactory Lettuce를 통한 Redis 연결 팩토리
   * @return RedisTemplate 객체
   */
  @Bean
  public RedisTemplate<String, Integer> integerRedisTemplate(
      LettuceConnectionFactory lettuceConnectionFactory) {
    RedisTemplate<String, Integer> template = new RedisTemplate<>();
    template.setConnectionFactory(lettuceConnectionFactory);
    template.setKeySerializer(RedisSerializer.string());
    template.setValueSerializer(RedisSerializer.json());
    return template;
  }
}

```

## RedisComponent 클래스

`RedisComponent` 클래스는 `RedisTemplate`과 `StringRedisTemplate`에 자주 사용되는 메서드인 `opsForValue()`,
`delete()`를 활용하여 Redis에 데이터 CRUD 작업을 위한 기본적인 메서드를 구현하였습니다.

### StringRedisTemplate에서 자주 사용되는 메서드

- **`opsForValue()`**: 문자열 값에 대한 작업을 수행할 수 있는 `ValueOperations` 객체를 제공합니다.
    - **`set(String key, String value)`**: 주어진 키에 값을 저장합니다.
    - **`get(String key)`**: 주어진 키에 저장된 값을 가져옵니다.
- **`delete(String key)`**: 주어진 키를 삭제합니다.
- **`hasKey(String key)`**: 주어진 키가 존재하는지 확인합니다.
- **`expire(String key, long timeout, TimeUnit unit)`**: 주어진 키의 만료 시간을 설정합니다.

### RedisTemplate<K, V>에서 자주 사용되는 메서드

- **`opsForValue()`**: 키-값 형태의 데이터를 처리하는 `ValueOperations`를 제공합니다.
    - **`set(K key, V value)`**: 주어진 키에 값을 저장합니다.
    - **`get(K key)`**: 주어진 키에 저장된 값을 가져옵니다.
- **`opsForHash()`**: 해시 데이터 구조에 대한 작업을 지원합니다.
    - **`put(H hashKey, HK fieldKey, HV value)`**: 해시 내 특정 필드에 값을 저장합니다.
    - **`get(H hashKey, Object fieldKey)`**: 해시 내 특정 필드의 값을 가져옵니다.
- **`opsForList()`**: 리스트 데이터 구조에 대한 작업을 지원합니다.
    - **`leftPush(K key, V value)`**: 리스트의 왼쪽(head)에 값을 추가합니다.
    - **`rightPop(K key)`**: 리스트의 오른쪽(tail) 값을 제거하고 반환합니다.
- **`opsForSet()`**: 집합 데이터 구조에 대한 작업을 지원합니다.
    - **`add(K key, V... values)`**: 집합에 하나 이상의 값을 추가합니다.
    - **`members(K key)`**: 집합의 모든 멤버를 가져옵니다.
- **`opsForZSet()`**: 정렬된 집합(주어진 우선순위에 따라 정렬되는 데이터) 작업을 지원합니다.
    - **`add(K key, V value, double score)`**: 정렬된 집합에 값을 추가합니다.
    - **`range(K key, long start, long end)`**: 특정 범위 내의 값을 가져옵니다.

```java
package com.example.redis.component;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisComponent {

  private final StringRedisTemplate stringRedisTemplate;
  private final RedisTemplate<String, Object> objectRedisTemplate;
  private final RedisTemplate<String, Integer> integerRedisTemplate;

  /**
   * 키에 해당하는 문자열을 저장
   *
   * @param key      저장할 키
   * @param value    저장할 값
   * @param duration 지속시간
   * @param timeunit 지속시간의 단위
   */
  public void setStringValue(String key, String value, long duration, TimeUnit timeunit) {
    try {
      ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
      valueOperations.set(key, value, duration, timeunit);
    } catch (Exception e) {
      log.error("setStringValue key: {}", key, e);
    }
  }

  /**
   * 키에 해당하는 문자열을 조회
   *
   * @param key 검색할 키
   * @return 해당 키로부터 가져온 값, 없거나 오류가 발생하면 null 반환
   */
  public String getStringValue(String key) {
    try {
      ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
      String value = valueOperations.get(key);
      if (StringUtils.isNotBlank(value)) {
        return value;
      }
    } catch (Exception e) {
      log.error("getStringValue key: {}", key, e);
    }
    return null;
  }

  /**
   * 키에 해당하는 객체를 JSON 문자열 형태로 저장
   *
   * @param key      저장할 키
   * @param value    저장할 객체
   * @param duration 지속시간
   * @param timeunit 지속시간의 단위
   */
  public void setObjectValue(String key, Object value, long duration, TimeUnit timeunit) {
    try {
      ValueOperations<String, Object> valueOperations = objectRedisTemplate.opsForValue();
      valueOperations.set(key, value, duration, timeunit);
    } catch (Exception e) {
      log.error("setObjectValue key: {}", key, e);
    }
  }

  /**
   * 키에 해당하는 JSON 문자열을 조회하여 객체로 반환
   *
   * @param key   검색할 키
   * @param clazz 반환할 객체의 클래스 타입
   * @param <T>   반환하는 객체의 타입
   * @return 해당 키로부터 가져온 객체, 없거나 오류가 발생하면 null 반환
   */
  public <T> T getObjectValue(String key, Class<T> clazz) {
    try {
      ValueOperations<String, Object> valueOperations = objectRedisTemplate.opsForValue();
      Object result = valueOperations.get(key);
      if (clazz.isInstance(result)) {
        return clazz.cast(result);
      }
    } catch (Exception e) {
      log.error("getObjectValue key: {}", key, e);
    }
    return null;
  }

  /**
   * 키에 해당하는 정수를 저장
   *
   * @param key      저장할 키
   * @param value    저장할 정수
   * @param duration 지속시간
   * @param timeunit 지속시간의 단위
   */
  public void setIntegerValue(String key, Integer value, long duration, TimeUnit timeunit) {
    try {
      ValueOperations<String, Integer> valueOperations = integerRedisTemplate.opsForValue();
      valueOperations.set(key, value, duration, timeunit);
    } catch (Exception e) {
      log.error("setIntegerValue key: {}", key, e);
    }
  }

  /**
   * 키에 해당하는 정수를 조회
   *
   * @param key 검색할 키
   * @return 해당 키로부터 가져온 정수 값, 없거나 오류가 발생하면 null 반환
   */
  public Integer getIntegerValue(String key) {
    try {
      ValueOperations<String, Integer> valueOperations = integerRedisTemplate.opsForValue();
      return valueOperations.get(key);
    } catch (Exception e) {
      log.error("getIntegerValue key: {}", key, e);
    }
    return null;
  }

  /**
   * 키에 해당하는 값을 삭제
   *
   * @param key 삭제할 키
   * @return 삭제가 성공했는지 여부
   */
  public boolean deleteKey(String key) {
    try {
      return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    } catch (Exception e) {
      log.error("deleteKey key : {}", key, e);
      return false;
    }
  }
}

```

## 테스트 케이스

`RedisComponent` 클래스에서 생성한 `stringRedisTemplate`, `objectRedisTemplate`, `integerRedisTemplate` 빈을
사용하여 Redis에서 데이터에 대한 CRUD 작업을 테스트합니다.

### 1. 문자열을 저장하고 조회

```java

@Test
void testStringSetAndGet() {

// Given
  String key = "SAMPLE:STR_100";
  String strRequest = "Hello, Redis!";
  long duration = 10L;
  TimeUnit timeUnit = TimeUnit.MINUTES;

// When
  redisComponent.setStringValue(key, strRequest, duration, timeUnit);

// Then
  String retrievedValue = redisComponent.getStringValue(key);
  log.debug("retrievedValue : {}", retrievedValue);

  assertAll(
      () -> assertNotNull(retrievedValue),
      () -> assertEquals(strRequest, retrievedValue)
  );
}
```

### 2. 객체를 JSON 문자열 형태로 저장하고 조회

```java

@Test
void testObjectSetAndGet() {

// Given
  String key = "SAMPLE:OBJECT_100";
  SampleDto sample = SampleDto.of("100", "Gildong", 30);
  long duration = 10L;
  TimeUnit timeUnit = TimeUnit.MINUTES;

// When
  redisComponent.setObjectValue(key, sample, duration, timeUnit);

// Then
  SampleDto retrievedSample = redisComponent.getObjectValue(key, SampleDto.class);
  log.debug("retrievedSample : {}", retrievedSample);

  assertAll(
      () -> assertNotNull(retrievedSample),
      () -> assertEquals(sample.getId(), retrievedSample.getId()),
      () -> assertEquals(sample.getName(), retrievedSample.getName()),
      () -> assertEquals(sample.getAge(), retrievedSample.getAge())
  );
}
```

### 3. 정수를 저장하고 조회

```java

@Test
void testIntegerSetAndGet() {

  // Given
  String key = "SAMPLE:INT_100";
  Integer intRequest = 12_345;
  long duration = 10L;
  TimeUnit timeUnit = TimeUnit.MINUTES;

  // When
  redisComponent.setIntegerValue(key, intRequest, duration, timeUnit);

  // Then
  Integer retrievedValue = redisComponent.getIntegerValue(key);
  log.debug("retrievedValue : {}", retrievedValue);

  assertAll(
      () -> assertNotNull(retrievedValue),
      () -> assertEquals(intRequest, retrievedValue)
  );
}
```

### 4. 만료 후 문자열 조회 시 null 반환

```java

@Test
void testExpirationOfString() throws InterruptedException {

  // Given
  String key = "SAMPLE:EXPIRING_STR_100";
  String strRequest = "Expiring String";
  long duration = 1L;
  TimeUnit timeUnit = TimeUnit.SECONDS;

  // When
  redisComponent.setStringValue(key, strRequest, duration, timeUnit);
  Thread.sleep(2000);

  // Then
  String retrievedValue = redisComponent.getStringValue(key);
  log.debug("retrievedValue : {}", retrievedValue);

  assertEquals(null, retrievedValue);
}
```

### 5. 동일한 키로 다른 타입 데이터 저장 시의 동작 확인

```java

@Test
void testOverwriteWithDifferentType() {

  // Given
  String key = "SAMPLE:OVERWRITE_100";
  String stringInitialValue = "Initial String";
  Integer intNewValue = 999;

  // When
  redisComponent.setStringValue(key, stringInitialValue, 10L, TimeUnit.MINUTES);
  String retrievedStringValue = redisComponent.getStringValue(key);

  redisComponent.setIntegerValue(key, intNewValue, 10L, TimeUnit.MINUTES);
  Integer retrievedIntValue = redisComponent.getIntegerValue(key);

  // Then
  log.debug("retrievedStringValue : {}", retrievedStringValue);
  log.debug("retrievedIntValue: {}", retrievedIntValue);

  assertAll(
      () -> assertNotNull(retrievedStringValue),
      () -> assertEquals(stringInitialValue, retrievedStringValue),
      () -> assertNotNull(retrievedIntValue),
      () -> assertEquals(intNewValue, retrievedIntValue)
  );
}
```

### 6. 문자열 삭제

```java

@Test
void testDeleteStringKey() {

  // Given
  String key = "SAMPLE:STR_101";
  String strValue = "Delete Test String";
  redisComponent.setStringValue(key, strValue, 10, TimeUnit.MINUTES);
  assertNotNull(redisComponent.getStringValue(key));

  // When
  boolean deleted = redisComponent.deleteKey(key);

  // Then
  assertAll(
      () -> assertTrue(deleted),
      () -> assertNull(redisComponent.getStringValue(key))
  );
}
```

### 7. 객체 삭제

```java

@Test
void testDeleteObjectKey() {

  // Given
  String key = "SAMPLE:OBJECT_101";
  SampleDto sample = SampleDto.of("101", "Gildong", 25);
  redisComponent.setObjectValue(key, sample, 10, TimeUnit.MINUTES);
  assertNotNull(redisComponent.getObjectValue(key, SampleDto.class));

  // When
  boolean deleted = redisComponent.deleteKey(key);

  // Then
  assertAll(
      () -> assertTrue(deleted),
      () -> assertNull(redisComponent.getObjectValue(key, SampleDto.class))
  );
}
```

### 8. 정수 삭제

```java

@Test
void testDeleteIntegerKey() {

  // Given
  String key = "SAMPLE:INT_101";
  Integer intValue = 6789;
  redisComponent.setIntegerValue(key, intValue, 10, TimeUnit.MINUTES);
  assertNotNull(redisComponent.getIntegerValue(key));

  // When
  boolean deleted = redisComponent.deleteKey(key);

  // Then
  assertAll(
      () -> assertTrue(deleted),
      () -> assertNull(redisComponent.getIntegerValue(key))
  );
}
```

## 참고 자료
- 

- [docker-redis-cluster GitHub Repository](https://github.com/Grokzen/docker-redis-cluster)
