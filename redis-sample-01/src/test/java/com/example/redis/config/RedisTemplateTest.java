package com.example.redis.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.ObjectUtils;

@Slf4j
@SpringBootTest
class RedisTemplateTest {

  @Autowired
  RedisTemplate redisTemplate;

  @DisplayName("Data Type이 String인 경우 테스트")
  @Test
  public void testString() {

    // Given
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    String key = "string_key";
    String value = "value_01";

    // When
    valueOperations.set(key, value);

    // Then
    String actualValue = valueOperations.get(key);
    log.debug("actualValue: {}", actualValue);
    assertEquals(value, actualValue);
  }

  @DisplayName("Data Type이 Set인 경우 테스트")
  @Test
  void testSet() {

    // Given
    SetOperations<String, String> setOperations = redisTemplate.opsForSet();
    String key = "set_key";
    String value01 = "value_01";
    String value02 = "value_02";
    String value03 = "value_03";

    // When
    setOperations.add(key, value01, value02, value03);

    // Then
    Set<String> members = setOperations.members(key);
    log.debug("members: {}", members);
    assertFalse(ObjectUtils.isEmpty(members));
    assertAll(
        () -> assertTrue(members.contains(value01)),
        () -> assertTrue(members.contains(value02)),
        () -> assertTrue(members.contains(value03))
    );
  }

  @DisplayName("Data Type이 Hash인 경우 테스트")
  @Test
  void testHash() {

    // Given
    HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
    String key = "hash_key";
    String hashKey = "field_01";
    String value = "value_01";

    // When
    hashOperations.put(key, hashKey, value);

    // Then
    Map<Object, Object> map = hashOperations.entries(key);
    log.debug("map: {}", map);
    assertAll(
        () -> assertTrue(map.keySet().contains(hashKey)),
        () -> assertTrue(map.values().contains(value))
    );
  }
}
