package com.example.redisson.common.config;

import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class RedissonConfig {

  @Value("${redis.stand-alone.host}")
  private String host;

  @Value("${redis.stand-alone.port}")
  private int port;

  /**
   * RedissonClient 빈 생성
   *
   * @return RedissonClient 객체
   */
  @Bean
  public RedissonClient redissonClient() {
    Config config = new Config();
    config.useSingleServer()                        // 싱글 Redis 서버 모드 사용
        .setAddress("redis://" + host + ":" + port) // Redis 서버 주소 (호스트와 포트)
        .setConnectionMinimumIdleSize(5)            // 최소 유휴 연결 수
        .setConnectionPoolSize(10)                  // 최대 커넥션 풀 크기
        .setIdleConnectionTimeout(10000)            // 유휴 연결 타임아웃 (ms)
        .setConnectTimeout(10000)                   // 연결 타임아웃 (ms)
        .setTimeout(3000)                           // 명령 실행 타임아웃 (ms)
        .setRetryAttempts(3)                        // 재시도 횟수
        .setRetryInterval(1500);                    // 재시도 간격 (ms)
    return Redisson.create(config);                 // RedissonClient 객체 생성 및 반환
  }

  /*@Value("${redis.cluster.nodes}")
  private List<String> nodes;

  @Bean
  public RedissonClient redissonClient() {
    Config config = new Config();
    config.useClusterServers()
        .addNodeAddress(nodes.stream()
            .map(node -> "redis://" + node)     // 노드를 "redis://" 형식으로 변환
            .toArray(String[]::new))                  // 클러스터 노드 주소 추가
        .setScanInterval(2000)                        // 클러스터 상태 확인 간격 (ms)
        .setReadMode(ReadMode.MASTER_SLAVE)           // 읽기 모드: Master-Slave
        .setSubscriptionMode(SubscriptionMode.MASTER) // 구독 작업: Master 노드에서만 수행
        .setTimeout(3000)                             // 타임아웃 설정 (ms)
        .setConnectTimeout(10_000)                    // 연결 타임아웃 (ms)
        .setRetryAttempts(3)                          // 재시도 횟수 ()
        .setRetryInterval(1500);                      // 재시도 간격 (ms)
    return Redisson.create(config);
  }*/
}
