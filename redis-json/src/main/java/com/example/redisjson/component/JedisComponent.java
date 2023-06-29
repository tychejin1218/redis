package com.example.redisjson.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.json.Path;

@Slf4j
@RequiredArgsConstructor
@Component
public class JedisComponent {

  private final JedisPooled jedisPooled;

  /**
   * 객체를 Redis에 JSON 형식으로 저장
   *
   * @param key Redis 키
   * @param t   저장할 데이터 객체
   * @param <T> 데이터 객체의 타입
   */
  public <T> void setJson(String key, T t) {
    jedisPooled.jsonSetLegacy(key, t);
  }

  /**
   * 키(key)를 사용하여 Redis에 저장된 객체를 조회
   *
   * @param key   Redis 키
   * @param clazz 가져올 데이터 객체의 클래스 타입
   * @param <T>   가져올 데이터 객체의 타입
   * @return Redis에서 가져온 데이터 객체
   */
  public <T> T getJsonObject(String key, Class<T> clazz) {
    return jedisPooled.jsonGet(key, clazz);
  }

  /**
   * 키(key)와 경로(path)를 사용하여 Redis에 저장된 객체를 조회
   *
   * @param key   Redis 키
   * @param clazz 가져올 데이터 객체의 클래스 타입
   * @param path  JSON 배열의 경로
   * @param <T>   가져올 데이터 객체의 타입
   * @return Redis에서 가져온 JSON 배열을 변환한 List 객체
   */
  public <T> List<T> getJsonArray(String key, Class<T> clazz, String path) throws IOException {

    List<T> list = new ArrayList<>();

    Object object = jedisPooled.jsonGet(key, new Path(path));
    if (!ObjectUtils.isEmpty(object)) {
      ObjectMapper objectMapper = new ObjectMapper();
      String jsonString = objectMapper.writeValueAsString(object);
      list = objectMapper.readValue(
          jsonString,
          objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    return list;
  }
}
