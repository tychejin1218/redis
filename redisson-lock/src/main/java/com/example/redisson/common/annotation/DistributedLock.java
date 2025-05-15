package com.example.redisson.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 분산 락 적용용 어노테이션
 * <p> - 특정 메서드 실행 시 Redisson 분산 락을 획득/반환하도록 처리
 */
@Target(ElementType.METHOD)         // 메서드에만 적용 가능
@Retention(RetentionPolicy.RUNTIME) // 런타임 시점 접근 가능
@Documented
public @interface DistributedLock {

  String key();                     // 락을 식별하기 위한 Redis key

  long waitTime() default 5;        // 락 획득 대기 시간 (초)

  long leaseTime() default 10;      // 락 점유 시간 (초)
}