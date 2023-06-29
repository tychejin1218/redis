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

  /**
   * GenericObjectPoolConfig 빈을 생성하여 반환
   *
   * @return JedisPooled 객체
   */
  @Bean
  public GenericObjectPoolConfig genericObjectPoolConfig() {
    return new GenericObjectPoolConfig();
  }

  /**
   * JedisPooled 빈을 생성하여 반환
   *
   * @return JedisPooled 객체
   */
  @Bean
  public JedisPooled jedisPooled(GenericObjectPoolConfig genericObjectPoolConfig) {
    JedisPooled jedisPooled = new JedisPooled(
        genericObjectPoolConfig,
        Protocol.DEFAULT_HOST,
        Protocol.DEFAULT_PORT);
    return jedisPooled;
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

//  @Value("${redis.cluster.nodes}")
//  private List<String> nodes;

//  @Bean
//  public JedisCluster jedisCluster(GenericObjectPoolConfig connectionPoolConfig) {
//
//    Set<HostAndPort> jedisClusterNodes = new HashSet<>();
//    nodes.forEach(node -> {
//      String[] nodeSplit = node.split(":");
//      jedisClusterNodes.add(new HostAndPort(nodeSplit[0], Integer.parseInt(nodeSplit[1])));
//    });
//
//    return new JedisCluster(jedisClusterNodes, connectionPoolConfig);
//  }
