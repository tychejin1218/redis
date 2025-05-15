# [Spring Boot] Redisson을 활용하여 분산 락 적용

## 1. Redisson이란?
Redisson은 Redis 기반의 Java 클라이언트 라이브러리로, 분산 데이터 구조, 분산 락, 메시징 큐, 캐시 등을 간편하게 구현할 수 있도록 지원합니다.
특히 Spring Boot와 같은 프레임워크와의 통합이 용이하며, 동시성 제어나 데이터 공유에 강력한 기능을 제공합니다.

### **Redisson의 주요 특징**
1. **분산 데이터 구조**: Redis의 다양한 데이터 타입(String, List, Set, Map 등)을 Java 객체처럼 다룰 수 있습니다.
2. **분산 락**: 여러 인스턴스가 경쟁하는 환경에서 안전하게 동시성 문제를 해결할 수 있습니다.
3. **Pub/Sub**: 이벤트 기반 메시징 큐 구현 가능.
4. **Spring 지원**: Spring Data Redis와 호환되며, Spring Boot Starter 제공으로 설정이 간편합니다.
5. **다양한 모드 지원**: Single, Master/Slave, Cluster, Replicated 등 다양한 Redis 운영 모드 지원.

### **분산 락 사용 시 자주 쓰는 메서드**
분산 락을 구현할 때 주로 사용하는 Redisson의 메서드들은 다음과 같습니다.

- **getLock(String name)**
  지정된 이름으로 RLock 객체를 생성하며, Redis의 key로 사용됩니다.
``` java
  RLock lock = redissonClient.getLock("lockKey");
```
- **tryLock(long waitTime, long leaseTime, TimeUnit unit)**
  주어진 시간 동안 락 획득을 시도하며, 성공하면 지정된 시간 후 자동으로 해제됩니다.
``` java
  boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
```
- **unlock()**
  현재 스레드가 보유한 락을 해제합니다. 반드시 finally 블록에서 호출해야 합니다.
``` java
  if (lock.isHeldByCurrentThread()) {
      lock.unlock();
  }
```
- **isHeldByCurrentThread()**
  현재 스레드가 해당 락을 보유하고 있는지 여부를 반환합니다.
``` java
  boolean myLock = lock.isHeldByCurrentThread();
```
실제 분산 락 구현에서는 `tryLock`으로 락 획득 성공 여부를 꼭 체크하고, 예외·실패 상황에 주의해 안전하게 락을 해제(lock.unlock())하는 것이 중요합니다.

---

## 2. Redis 서버 준비

Docker를 통해 로컬에 Redis 서버를 설치하고 실행할 수 있습니다.
```groovy
docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
```
* 포트 6379: Redis 클라이언트 접속용
* 포트 8001: RedisInsight(웹 UI) 접속용

---

## 3. Spring Boot 프로젝트 설정

### 2.1 build.gradle 의존성 추가
Redisson을 포함한 Spring Boot 프로젝트 의존성을 설정합니다.

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.4.5'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.example.redisson'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {

    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-aop'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'

    // Redisson
    implementation 'org.redisson:redisson-spring-boot-starter:3.46.0'

    // ModelMapper
    implementation 'org.modelmapper:modelmapper:3.2.3'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testCompileOnly 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

### 3.2 application.yml Redis 정보 설정

```yaml
# Redis 설정
redis:
  stand-alone:
    host: localhost
    port: 6379
```

---

## 4. Redisson 분산 락 구현

### 4.1 RedisConfig 클래스
RedissonClient를 생성하여 Redis와 연결하는 설정 클래스입니다.

```java
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
}
```

### 4.2 분산 락 적용을 위한 커스텀 어노테이션
메서드 단위로 분산 락을 적용할 때 사용하는 어노테이션입니다.

```java
package com.example.redisson.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 분산 락 적용용 어노테이션
 * <p>
 * 특정 메서드 실행 시 Redisson 분산 락을 획득/반환하도록 처리
 */
@Target(ElementType.METHOD)         // 메서드에만 적용 가능
@Retention(RetentionPolicy.RUNTIME) // 런타임 시점 접근 가능
@Documented
public @interface DistributedLock {

  String key();                     // 락을 식별하기 위한 Redis key

  long waitTime() default 5;        // 락 획득 대기 시간 (초)

  long leaseTime() default 10;      // 락 점유 시간 (초)
}
```

### 4.3 분산 락 AOP 구현
어노테이션이 적용된 메서드 실행 시점에 락 획득/해제를 자동으로 수행합니다.

```java
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
```

---

## 5. 비즈니스 로직 예제

### LockService 클래스
분산 락이 적용된 작업 예제입니다.

```java
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
```

---

## 6. 동시성 테스트

다중 스레드 환경에서 Redisson 기반 분산 락이 올바르게 동작하는지를 검증하기 위한 테스트 케이스입니다.
단위 테스트에서는 모킹(mock)을 활용하여 예외 상황까지 검증하고, 통합 테스트에서는 실제 스레드 환경에서 락의 동시성 제어를 검증합니다.

### 6.1 Mockito 기반 단위 테스트

RedissonClient와 RLock을 모킹하여 락 획득 성공/실패 및 인터럽트 상황 등을 테스트합니다.

```java
package com.example.redisson.lock.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class LockServiceUnitTest {

  @Autowired
  LockService lockService;

  @MockitoBean
  RedissonClient redissonClient;

  private final String LOCK_KEY = "sampleLock";
  private final long WAIT_TIME = 5;
  private final long LEASE_TIME = 10;

  @Order(1)
  @DisplayName("락 획득 성공 시 정상적으로 메서드가 실행되고, 락 해제가 호출되는지 확인")
  @Test
  void testExecuteWithLock_Success() throws Exception {

    // Given: RLock 객체 생성 및 mock 설정
    RLock mockLock = Mockito.mock(RLock.class);
    when(redissonClient.getLock(LOCK_KEY)).thenReturn(mockLock);
    when(mockLock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenReturn(true);
    when(mockLock.isHeldByCurrentThread()).thenReturn(true);

    // When: 락이 성공적으로 획득된 상태에서 메서드 실행
    lockService.executeWithLock();

    // Then: tryLock()이 정확히 1번 호출되고, unlock()도 호출되었는지 확인
    assertAll(
        () -> verify(mockLock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS),
        () -> verify(mockLock, times(1)).unlock()
    );
  }

  @Order(2)
  @DisplayName("락 획득 실패 시 `IllegalStateException`이 발생하는지 확인")
  @Test
  void testExecuteWithLock_FailToAcquireLock() throws Exception {

    // Given: RLock 객체 생성 및 mock 설정
    RLock mockLock = Mockito.mock(RLock.class);
    when(redissonClient.getLock(LOCK_KEY)).thenReturn(mockLock);
    // 락 획득 실패 설정
    when(mockLock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenReturn(false);

    // When: 락 획득 실패 상태에서 메서드 실행
    Exception exception = assertThrows(IllegalStateException.class,
        () -> lockService.executeWithLock());

    // Then: 예외 발생 여부, tryLock 호출 횟수, unlock 미호출 여부를 확인
    assertAll(
        () -> verify(mockLock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS),
        () -> verify(mockLock, never()).unlock(),
        () -> assertNotNull(exception),
        () -> assertFalse(mockLock.isHeldByCurrentThread())
    );
  }

  @Order(3)
  @DisplayName("락 대기 중 인터럽트 발생 시 `IllegalStateException`이 발생하는지 확인")
  @Test
  void testExecuteWithLock_InterruptedException() throws Exception {

    // Given: RLock 객체 생성 및 mock 설정
    RLock mockLock = Mockito.mock(RLock.class);
    when(redissonClient.getLock(LOCK_KEY)).thenReturn(mockLock);
    when(mockLock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenThrow(
        new InterruptedException());

    // When: 인터럽트 발생 상태에서 메서드 실행
    Exception exception = assertThrows(IllegalStateException.class,
        () -> lockService.executeWithLock());

    // Then: tryLock 호출 여부, unlock 미수행 여부, 예외 객체 유무를 확인
    assertAll(
        () -> verify(mockLock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS),
        () -> verify(mockLock, never()).unlock(),
        () -> assertNotNull(exception)
    );
  }
}
```

이 테스트는 Redisson 객체를 실제로 생성하지 않고, 모의 객체를 통해 락 동작을 검증합니다.
락을 획득했을 때의 정상 처리뿐 아니라, 실패 및 예외 상황에서도 unlock() 호출 여부 등 중요한 흐름을 테스트할 수 있습니다.

### 6_2. 통합 테스트 코드

실제 Redisson 인스턴스를 활용하여, 여러 스레드가 동시에 동일한 자원에 접근할 때 락이 제대로 동작하는지를 검증합니다.


```java
package com.example.redisson.lock.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class LockServiceIntegrationTest {

  @Autowired
  LockService lockService;

  @DisplayName("멀티스레드 환경에서 하나의 스레드만 락 획득에 성공하고, 나머지는 모두 실패하는지 검증")
  @Test
  void testExecuteWithLock_WhenMultipleThreads_OnlyOneSuccess() throws Exception {

    // Given: 5개의 쓰레드로 구성된 고정된 쓰레드 풀과 동기화를 위한 CountDownLatch 준비
    int threadCount = 5;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);

    // When: 여러 스레드가 동시에 락 획득 시도
    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          lockService.executeWithLock();
          successCount.incrementAndGet();
        } catch (Exception e) {
          failCount.incrementAndGet();
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executorService.shutdown();

    // Then: 하나의 스레드만 락 획득에 성공하고, 나머지는 모두 실패하는지 검증
    assertAll(
        () -> assertEquals(1, successCount.get()),
        () -> assertEquals(threadCount - 1, failCount.get())
    );
  }
}
```

ExecutorService, CountDownLatch를 사용해 동시에 다수의 스레드가 락을 획득하도록 시도합니다.
분산 락이 정확하게 작동할 경우, 오직 하나의 스레드만 락을 획득해 성공하고, 나머지는 모두 실패해야 합니다.

---

## 7. 참고 자료

- [Redisson GitHub 공식 문서](https://github.com/redisson/redisson)
- [Redisson Reference Guide](https://redisson.pro/docs/)
