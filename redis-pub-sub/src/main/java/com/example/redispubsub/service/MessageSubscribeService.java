package com.example.redispubsub.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageSubscribeService implements MessageListener {

  /**
   * 메시지 수신 시 호출되는 콜백
   *
   * @param message 수신된 메시지
   * @param pattern 구독한 패턴
   */
  public void onMessage(Message message, byte[] pattern) {
    log.info("Message received: {}", message);
  }
}
