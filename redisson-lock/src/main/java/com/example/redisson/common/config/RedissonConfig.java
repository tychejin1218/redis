package com.example.redisson.common.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@RequiredArgsConstructor
@Configuration
@Profile("redis_cluster")
public class RedissonConfig {

  @Value("${redis.cluster.nodes}")
  private List<String> nodes;

  /**
   * RedissonClient 빈 생성
   *
   * @return RedissonClient 객체
   */
  @Bean
  public RedissonClient redissonClient() {
    Config config = new Config();
    config.useClusterServers()
        .addNodeAddress(nodes.stream()
            .map(node -> "redis://" + node)
            .toArray(String[]::new))                  // Redis 클러스터 노드 주소 추가
        .setScanInterval(2000)                        // 클러스터 상태 확인 간격 설정 (2초)
        .setReadMode(ReadMode.MASTER_SLAVE)           // 읽기 모드: Master-Slave
        .setSubscriptionMode(SubscriptionMode.MASTER) // 구독 처리: Master 노드에서만
        .setTimeout(3000)                             // 클라이언트 타임아웃 설정 (3초)
        .setConnectTimeout(10_000)                    // Redis 연결 타임아웃 설정 (10초)
        .setRetryAttempts(3)                          // 실패 시 재시도 횟수 설정 (3번)
        .setRetryInterval(1500);                      // 재시도 간격 설정 (1.5초)
    return Redisson.create(config);
  }
}
