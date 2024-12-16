package com.example.redis.component;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class RedisComponentTest {

  @Autowired
  RedisComponent redisComponent;

  @Order(1)
  @DisplayName("문자열을 저장하고 조회")
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

  @Order(2)
  @DisplayName("객체를 JSON 문자열 형태로 저장하고 조회")
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

  @Order(3)
  @DisplayName("정수를 저장하고 조회")
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

  @Order(4)
  @DisplayName("만료 후 문자열 조회 시 null 반환")
  @Test
  void testExpirationOfString() throws InterruptedException {

    // Given
    String key = "SAMPLE:EXPIRING_STR_100";
    String strRequest = "Expiring String";
    long duration = 1L;
    TimeUnit timeUnit = TimeUnit.SECONDS;

    // When
    redisComponent.setStringValue(key, strRequest, duration, timeUnit);
    TimeUnit.SECONDS.sleep(duration);

    // Then
    String retrievedValue = redisComponent.getStringValue(key);
    log.debug("retrievedValue : {}", retrievedValue);

    assertEquals(null, retrievedValue);
  }

  @Order(5)
  @DisplayName("동일한 키로 다른 타입 데이터 저장 시의 동작 확인")
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

  @Order(6)
  @DisplayName("문자열 삭제")
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

  @Order(7)
  @DisplayName("객체 삭제")
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

  @Order(8)
  @DisplayName("정수 삭제")
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

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor(staticName = "of")
  @ToString
  public static class SampleDto {

    private String id;
    private String name;
    private int age;
  }
}
