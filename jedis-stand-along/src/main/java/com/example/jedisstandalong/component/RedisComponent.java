package com.example.jedisstandalong.component;

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
public class RedisComponent {

  private final JedisPooled jedisPooled;

  /**
   * Redis에 객체를 JSON 형식으로 저장
   *
   * @param key     Redis 키
   * @param t       저장할 데이터 객체
   * @param seconds 만료 시간(초)
   * @param <T>     데이터 객체의 유형
   */
  public <T> void setJson(String key, T t, long seconds) {
    jedisPooled.jsonSetLegacy(key, t);
    jedisPooled.expire(key, seconds);
  }

  /**
   * Redis에서 키를 사용하여 저장된 객체를 조회
   *
   * @param key   Redis 키
   * @param clazz 검색할 데이터 객체의 클래스 유형
   * @param <T>   검색할 데이터 객체의 유형
   * @return Redis에서 검색한 데이터 객체
   */
  public <T> T getJson(String key, Class<T> clazz) {
    return jedisPooled.jsonGet(key, clazz);
  }


  /**
   * 키와 경로를 사용하여 Redis에서 저장된 객체를 조회
   *
   * @param key  Redis 키
   * @param path 가져올 데이터 객체의 경로
   * @return Redis에서 검색한 데이터 객체
   */
  public Object getJson(String key, Path path) {
    return jedisPooled.jsonGet(key, path);
  }


  /**
   * Redis에서 저장된 JSON 배열을 조회
   *
   * @param key   Redis에서의 키
   * @param clazz 검색할 데이터 객체의 클래스 유형
   * @param path  JSON 배열의 경로
   * @param <T>   검색할 데이터 객체의 유형
   * @return 검색된 데이터 객체의 목록
   * @throws IOException JSON을 파싱하는 동안 발생할 수 있는 예외
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
