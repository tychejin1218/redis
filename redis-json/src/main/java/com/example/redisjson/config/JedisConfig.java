package com.example.redisjson.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPooled;

@Slf4j
@Configuration
public class JedisConfig {

  @Value("${redis.single.node}")
  private String node;

//  @Value("${redis.cluster.nodes}")
//  private List<String> nodes;

  @Bean
  public GenericObjectPoolConfig connectionPoolConfig() {
    return new GenericObjectPoolConfig();
  }

  @Bean
  public JedisPooled jedisPooled(GenericObjectPoolConfig connectionPoolConfig) {
    String[] nodeSplit = node.split(":");
    JedisPooled jedisPooled = new JedisPooled(
        connectionPoolConfig, nodeSplit[0], Integer.parseInt(nodeSplit[1]));
    return jedisPooled;
  }

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
}
