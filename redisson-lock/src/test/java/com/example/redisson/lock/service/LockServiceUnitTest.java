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

  private final String USER_ID = "testLockKey";
  private final long WAIT_TIME = 5;
  private final long LEASE_TIME = 10;

  @Order(1)
  @DisplayName("락 획득 성공 시 정상적으로 메서드가 실행되고, 락 해제가 호출되는지 확인")
  @Test
  void testExecuteWithLock_Success() throws Exception {

    // Given: RLock 객체 생성 및 mock 설정
    RLock mockLock = Mockito.mock(RLock.class);
    when(redissonClient.getLock(USER_ID)).thenReturn(mockLock);
    when(mockLock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenReturn(true);
    when(mockLock.isHeldByCurrentThread()).thenReturn(true);

    // When: 락이 성공적으로 획득된 상태에서 메서드 실행
    lockService.executeWithLock(USER_ID);

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
    when(redissonClient.getLock(USER_ID)).thenReturn(mockLock);
    // 락 획득 실패 설정
    when(mockLock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenReturn(false);

    // When: 락 획득 실패 상태에서 메서드 실행
    Exception exception = assertThrows(IllegalStateException.class,
        () -> lockService.executeWithLock(USER_ID));

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
    when(redissonClient.getLock(USER_ID)).thenReturn(mockLock);
    when(mockLock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)).thenThrow(
        new InterruptedException());

    // When: 인터럽트 발생 상태에서 메서드 실행
    Exception exception = assertThrows(IllegalStateException.class,
        () -> lockService.executeWithLock(USER_ID));

    // Then: tryLock 호출 여부, unlock 미수행 여부, 예외 객체 유무를 확인
    assertAll(
        () -> verify(mockLock, times(1)).tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS),
        () -> verify(mockLock, never()).unlock(),
        () -> assertNotNull(exception)
    );
  }
}
