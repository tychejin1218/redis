package com.example.redisjson.config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Connection;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

@Slf4j
@Configuration
public class JedisConfig {

  @Value("${redis.cluster.nodes}")
  private List<String> nodes;

  /**
   * GenericObjectPoolConfig 빈을 생성하여 반환
   *
   * @return GenericObjectPoolConfig&lt;Connection&lt;
   */
  @Bean
  public GenericObjectPoolConfig<Connection> genericObjectPoolConfig() {
    return new GenericObjectPoolConfig<>();
  }

  /**
   * JedisCluster 빈을 생성하여 반환
   *
   * @return JedisCluster
   */
  @Bean
  public JedisCluster jedisCluster(GenericObjectPoolConfig<Connection> genericObjectPoolConfig) {

    Set<HostAndPort> jedisClusterNodes = nodes.stream()
        .map(node -> {
          String[] nodeSplit = node.split(":");
          return new HostAndPort(nodeSplit[0], Integer.parseInt(nodeSplit[1]));
        })
        .collect(Collectors.toSet());

    return new JedisCluster(jedisClusterNodes, genericObjectPoolConfig);
  }
}

//  @Value("${redis.single.node}")
//  private String node;
//
//  @Bean
//  public JedisPooled jedisPooled(GenericObjectPoolConfig genericObjectPoolConfig) {
//    String[] nodeSplit = node.split(":");
//    JedisPooled jedisPooled = new JedisPooled(
//        genericObjectPoolConfig, nodeSplit[0], Integer.parseInt(nodeSplit[1]));
//    return jedisPooled;
//  }
