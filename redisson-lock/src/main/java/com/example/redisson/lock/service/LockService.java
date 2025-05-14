package com.example.redisson.lock.service;

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
