package com.example.redisson.common.aspect;

import com.example.redisson.common.annotation.DistributedLock;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * 분산 락 적용을 위한 AOP 클래스
 * <p> - 메서드 실행 전 락 획득 시도, 후 락 해제
 * <p> - SpEL 표현식으로 동적 락 키 생성을 지원
 */
@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class DistributedLockAspect {

  private final RedissonClient redissonClient;
  private final ExpressionParser parser = new SpelExpressionParser();

  /**
   * 메서드 실행 전 락을 획득하고, 실행 후 해제
   *
   * @param joinPoint       실행 대상 메서드
   * @param distributedLock 락 설정 어노테이션
   * @return 메서드 실행 결과
   * @throws Throwable 예외 발생 시
   */
  @Around("@annotation(distributedLock)")
  public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock)
      throws Throwable {

    String lockKey = parseKey(joinPoint, distributedLock.key());
    long waitTime = distributedLock.waitTime();
    long leaseTime = distributedLock.leaseTime();

    RLock rLock = redissonClient.getLock(lockKey);
    boolean lockAcquired = false;

    try {
      lockAcquired = rLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
      if (!lockAcquired) {
        log.warn("[RedissonLock] 락 획득 실패 - lockKey: {}", lockKey);
        throw new IllegalStateException("락 획득 실패 - lockKey: " + lockKey);
      }
      log.info("[RedissonLock] 락 획득 성공 - lockKey: {}", lockKey);
      return joinPoint.proceed();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("락 획득 중 인터럽트 발생", e);
    } finally {
      if (lockAcquired && rLock.isHeldByCurrentThread()) {
        try {
          rLock.unlock();
          log.info("[RedissonLock] 락 해제 완료 - lockKey: {}", lockKey);
        } catch (IllegalMonitorStateException e) {
          log.warn("[RedissonLock] 이미 해제된 락 또는 스레드 불일치 - lockKey: {}", lockKey, e);
        }
      }
    }
  }

  /**
   * SpEL 표현식을 기반으로 락 키를 생성
   *
   * @param joinPoint     메서드 실행 정보
   * @param keyExpression SpEL 표현식
   * @return 생성된 락 키
   */
  private String parseKey(ProceedingJoinPoint joinPoint, String keyExpression) {
    EvaluationContext context = new StandardEvaluationContext();
    Object[] args = joinPoint.getArgs();
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String[] parameterNames = signature.getParameterNames();

    for (int i = 0; i < parameterNames.length; i++) {
      context.setVariable(parameterNames[i], args[i]);
    }

    return parser.parseExpression(keyExpression).getValue(context, String.class);
  }
}
