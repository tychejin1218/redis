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
