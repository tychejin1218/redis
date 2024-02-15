package com.example.jedissample.service;

import com.example.jedissample.component.RedisComponent;
import com.example.jedissample.dto.StoreDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.json.Path;

/**
 * Redis JSON data type overview
 *
 * <p>
 * https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/json-document-overview.html
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class StoreService {

  private final RedisComponent redisComponent;

  /**
   * Redis에 객체를 JSON(ReJSON-RL) 형태로 저장
   *
   * @param key          Redis 키
   * @param requestStore StoreDto
   * @return StoreDto
   */
  public StoreDto saveStore(String key, StoreDto requestStore) {
    redisComponent.setJson(key, requestStore, 60 * 5);
    return redisComponent.getJson(key, StoreDto.class);
  }

  /**
   * 키(key)를 사용하여 Redis에 저장된 객체를 조회
   *
   * @param key Redis 키
   * @return StoreDto
   */
  public StoreDto findStore(String key) {
    return redisComponent.getJson(key, StoreDto.class);
  }

  /**
   * 키(key)와 경로(path)를 사용하여 Redis에 저장된 객체를 조회
   *
   * @param key  Redis 키
   * @param path 경로
   * @return Object
   */
  public Object findStore(String key, Path path) {
    return redisComponent.getJson(key, path);
  }
}
