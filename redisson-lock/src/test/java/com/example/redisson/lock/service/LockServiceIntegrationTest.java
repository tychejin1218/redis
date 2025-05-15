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

  private final String USER_ID = "user:tester";

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
          lockService.executeWithLock(USER_ID);
          successCount.incrementAndGet();
        } catch (Exception e) {
          failCount.incrementAndGet();
          e.printStackTrace();
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
