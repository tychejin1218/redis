package com.example.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  /**
   * LettuceConnectionFactory를 사용하여 RedisConnectionFactory 빈을 생성하여 반환
   *
   * @return RedisConnectionFactory 객체
   */
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(new RedisStandaloneConfiguration());
  }

  /**
   * RedisTemplate 빈을 생성하여 반환
   *
   * @return RedisTemplate 객체
   */
  @Bean
  public RedisTemplate<?, ?> redisTemplate() {
    RedisTemplate<byte[], byte[]> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory());
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());
    return redisTemplate;
  }
}
