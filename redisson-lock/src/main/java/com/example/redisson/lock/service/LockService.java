package com.example.redisson.lock.service;

import com.example.redisson.common.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 비즈니스 로직에 분산 락을 적용한 서비스 클래스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class LockService {

  /**
   * 분산 락을 이용한 처리 메서드
   * <p> -동시에 여러 스레드가 접근하면 하나만 실행되고 나머지는 실패 처리됨
   */
  @DistributedLock(key = "#userId", waitTime = 5, leaseTime = 10)
  public void executeWithLock(String userId) {
    log.info("락 획득 후 작업 실행");
    try {
      Thread.sleep(5000); // 락 점유 시간 확보
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    log.info("락을 점유한 작업 종료");
  }
}
