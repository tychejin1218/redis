package com.example.redisson.common.aspect;

import com.example.redisson.common.annotation.DistributedLock;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class DistributedLockAspect {

  private final RedissonClient redissonClient;

  @Around("@annotation(lock)")
  public Object around(ProceedingJoinPoint joinPoint, DistributedLock lock) throws Throwable {

    String key = lock.key();
    long waitTime = lock.waitTime();
    long leaseTime = lock.leaseTime();

    RLock rLock = redissonClient.getLock(key);
    boolean acquired = false;

    try {
      acquired = rLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
      if (acquired) {
        log.info("[RedissonLock] Lock acquired: {}", key);
        return joinPoint.proceed();
      } else {
        log.warn("[RedissonLock] Failed to acquire lock: {}", key);
        throw new IllegalStateException("락 획득 실패: " + key);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("락 획득 중 인터럽트 발생", e);
    } finally {
      if (acquired && rLock.isHeldByCurrentThread()) {
        rLock.unlock();
        log.info("[RedissonLock] Lock released: {}", key);
      }
    }
  }
}
