# [Spring Boot] Redisson을 이용한 분산 락 활용

## 1. Redis Cluster 환경 구성

### 1.1 Docker를 이용한 Redis Cluster 환경 설정

Redis 클러스터는 `grokzen/redis-cluster` Docker 이미지를 활용하여 구성할 수 있습니다.

#### `docker-compose.yml`

### 1.2 환경 변수 설명
- **IP, BIND_ADDRESS**: Redis 인스턴스가 특정 IP에 바인딩되도록 설정. `0.0.0.0`은 모든 네트워크 인터페이스에서 접근 가능.
- **INITIAL_PORT**: Redis 클러스터의 시작 포트.
- **MASTERS, SLAVES_PER_MASTER**: 마스터와 슬레이브 노드 개수 설정.
- **포트 매핑**: `7001-7006` 범위를 로컬 포트와 컨테이너 포트로 매핑하여 모든 Redis 노드에 접근 가능.

---

## 2. Spring Redis 설정

### 2.1 Redis EndPoint 설정
Redis 클러스터의 노드 주소를 `application.yml` 파일에 추가하여 Spring에서 연결할 수 있도록 설정합니다.

### 2.2 build.gradle 의존성 추가
Spring Boot에서 Redisson을 활용하기 위한 의존성을 추가합니다.

---

## 3. Redisson을 이용한 분산 락 구현

### 3.1 RedisConfig 클래스
Redis 클러스터와의 연결 및 설정을 담당하는 RedissonClient를 생성합니다.

### 3.2 @DistributedLock 어노테이션
메서드 단위에서 락을 적용하기 위한 어노테이션을 정의합니다.

### 3.3 DistributedLockAspect 클래스
AOP를 통해 락의 적용과 해제를 자동으로 처리합니다.

---

## 4. 비즈니스 로직 예제

### 4.1 LockService 클래스
분산 락이 적용된 작업을 정의합니다.

### 4.2 LockController 클래스
LockService를 호출하는 엔드포인트를 정의합니다.

---

## 5. 동시성 테스트

아래는 다중 스레드 환경에서 락이 제대로 동작하는지 검증하는 테스트 케이스입니다.

---

## 6. 참고 자료

- [Redisson GitHub 공식 문서](https://github.com/redisson/redisson/wiki)
- [Redis 클러스터 공식 문서](https://redis.io/docs/management/scaling/)










### docker-compose.xml

```yaml
version: '3'
services:
  redis-cluster:
    container_name: redis-cluster-6
    image: grokzen/redis-cluster:7.0.15
    environment:
      - IP=0.0.0.0
      - BIND_ADDRESS=0.0.0.0
      - INITIAL_PORT=7001
      - MASTERS=3
      - SLAVES_PER_MASTER=1
    ports:
      - "7001-7006:7001-7006"
```

- **환경 변수**:
    - `IP`와 `BIND_ADDRESS`: Redis 인스턴스가 특정 IP에 바인딩되게 설정합니다. `0.0.0.0`으로 설정하면 모든 네트워크 인터페이스에서 접근 가능하게
      만듭니다.
    - `INITIAL_PORT`: 클러스터의 시작 포트를 지정합니다.
    - `MASTERS`: 클러스터 내 마스터 노드의 개수를 설정합니다.
    - `SLAVES_PER_MASTER`: 각 마스터 노드당 슬레이브 노드의 개수를 설정합니다.

- **포트 매핑**:
    - `7001-7006:7001-7006`은 로컬의 포트 7001에서 7006까지를 컨테이너의 동일한 포트로 매핑하여 각 클러스터의 각 노드에 접근할 수 있습니다.

## Redis EndPoint 추가

Redis 클러스터에 연결할 때 필요한 클러스터의 엔드포인트(예: 노드 중 하나의 IP와 포트)를 `application.yml` 파일에 추가합니다.

```yaml
# Redis 설정
redis:
  cluster:
    nodes:
      127.0.0.1:7001
```
---

## Redisson 연동 및 구현

### 1. build.gradle 의존성 추가

```yaml
  // Redisson
    implementation 'org.redisson:redisson-spring-boot-starter:3.46.0'
```

### 2. RedisConfig 클래스
**목적**: Redis 클러스터 연결을 위한 설정을 지원합니다.

```java
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
```

### 3. DistributedLock 인터페이스
**목적**: 메서드에서 분산 락을 관리하게 하는 인터페이스입니다.


```java
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    String key();

    long waitTime() default 5;

    long leaseTime() default 10;
}
```

### 4. DistributedLockAspect 클래스
**목적**: AOP를 통해 메서드 실행 전후에 분산 락을 설정 및 해제합니다.


```java
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
```

### 5. LockService 클래스
**목적**: 분산 락 사용 로직을 처리하는 서비스입니다.


```java
import com.example.redisson.common.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LockService {

    @DistributedLock(key = "sampleLock", waitTime = 5, leaseTime = 10)
    public void executeWithLock() throws InterruptedException {
        log.info("락 획득 후 작업 실행 중...");
        Thread.sleep(5000); // 작업 처리 시뮬레이션
        log.info("작업 완료 후 반환");
    }
}
```

### 6. LockController 클래스
**목적**: LockService를 호출하는 컨트롤러 역할을 수행합니다.


```java
import com.example.redisson.lock.service.LockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LockController {

    private final LockService lockService;

    @GetMapping("/lock")
    public String testLock() {
        try {
            lockService.executeWithLock();
            return "SUCCESS";
        } catch (Exception e) {
            return "FAILED: " + e.getMessage();
        }
    }
}

```

## 테스트 케이스 작성

### 1. 분산 락 테스트
**설명**: 아래는 분산 락 기능을 테스트하는 코드입니다.

```java
import com.example.redisson.lock.service.LockService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest
class LockControllerTest {

  @Autowired
  LockService lockService;

  @Test
  void testConcurrentLocking() throws InterruptedException {

    int threadCount = 5;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          lockService.executeWithLock();
        } catch (Exception e) {
          log.info("LOCK FAIL: {}", e.getMessage());
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executorService.shutdown();
  }
}
```

## 참고 자료
- Redisson 공식 문서: https://github.com/redisson/redisson/wiki
- Redis 클러스터 문서: https://redis.io/docs/management/scaling/
