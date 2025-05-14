package com.example.redisson.lock.controller;

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