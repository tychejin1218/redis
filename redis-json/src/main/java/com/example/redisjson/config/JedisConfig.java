package com.example.redisjson.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Protocol;

@Slf4j
@Configuration
public class JedisConfig {

  @Bean
  public GenericObjectPoolConfig connectionPoolConfig() {
    return new GenericObjectPoolConfig();
  }

  @Bean
  public JedisPooled jedisPooled(GenericObjectPoolConfig connectionPoolConfig) {
    JedisPooled jedisPooled = new JedisPooled(
        connectionPoolConfig,
        Protocol.DEFAULT_HOST,
        Protocol.DEFAULT_PORT);
    return jedisPooled;
  }
}
