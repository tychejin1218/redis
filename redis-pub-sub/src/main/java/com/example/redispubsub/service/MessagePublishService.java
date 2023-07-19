package com.example.redispubsub.service;

import com.example.redispubsub.dao.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessagePublishService {

  private final RedisTemplate<String, Object> redisTemplate;

  private final ChannelTopic channelTopic;

  /**
   * 메시지를 발행
   *
   * @param messageDto 발행할 메시지를 담은 MessageDto 객체
   * @return 발행 결과를 나타내는 Long 값 (0 이상의 값은 성공을 의미)
   */
  public Long publish(MessageDto messageDto) {
    log.info("Sending Message: {}", messageDto.toString());

    Long result = redisTemplate.convertAndSend(channelTopic.getTopic(), messageDto);
    if (result > 0) {
      log.info("Message published successfully");
    } else {
      log.warn("Failed to publish message or no subscribers found");
    }

    return result;
  }
}
