package com.example.jediscluster.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisCluster;

@Configuration
public class RedisConfig {

  @Value("${redis.cluster.nodes}")
  private List<String> clusterNodes;

  /**
   * RedisConnectionFactory 빈을 생성
   *
   * @return RedisClusterConfiguration 빈
   */
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new JedisConnectionFactory(new RedisClusterConfiguration(clusterNodes));
  }

  /**
   * RedisConnectionFactory를 사용하여 JedisCluster 빈을 생성
   *
   * @param redisConnectionFactory RedisConnectionFactory
   * @return JedisCluster 빈
   */
  @Bean
  public JedisCluster jedisCluster(RedisConnectionFactory redisConnectionFactory) {
    return (JedisCluster) redisConnectionFactory.getClusterConnection().getNativeConnection();
  }
}
