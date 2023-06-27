package com.example.redisjson.service;

import com.example.redisjson.config.JedisConfig;
import com.example.redisjson.dto.StoreDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.json.Path;

@Slf4j
@RequiredArgsConstructor
@Service
public class StoreService {

  private final JedisPooled jedisPooled;

  /**
   * Redis에 객체를 JSON(ReJSON-RL) 형태로 저장
   *
   * @param key           Redis JSON 키
   * @param requestStore  저장할 객체 (StoreDto)
   * @return              저장된 객체 (StoreDto)
   */
  public StoreDto saveStore(String key, StoreDto requestStore) {
    jedisPooled.jsonSetLegacy(key, requestStore);
    return jedisPooled.jsonGet(key, StoreDto.class);
  }

  /**
   * 키(key)를 사용하여 Redis에 저장된 객체를 조회
   *
   * @param key  Redis JSON 키
   * @return 경로에 해당하는 객체 (Object)
   */
  public StoreDto findStore(String key) {
    return jedisPooled.jsonGet(key, StoreDto.class);
  }

  /**
   * 키(key)와 경로(path)를 사용하여 Redis에 저장된 객체를 조회
   *
   * @param key  Redis JSON 키
   * @param path 경로
   * @return 경로에 해당하는 객체 (Object)
   */
  public Object findStore(String key, Path path) {
    return jedisPooled.jsonGet(key, path);
  }
}
