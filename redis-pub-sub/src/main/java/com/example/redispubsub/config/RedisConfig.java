package com.example.redispubsub.config;

import com.example.redispubsub.service.MessageSubscribeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
public class RedisConfig {

  /**
   * Ridis Pub/Sub 메시징을 위한 토픽
   */
  @Value("${redis.pubsub.topic}")
  private String redisPubSubTopic;

  /**
   * Redis 토픽을 나타내는 ChannelTopic 인스턴스를 생성
   *
   * @return Redis 토픽을 나타내는 ChannelTopic 객체
   */
  @Bean
  public ChannelTopic channelTopic() {
    return new ChannelTopic(redisPubSubTopic);
  }

  /**
   * Redis 연결을 위한 RedisConnectionFactory 빈을 생성
   *
   * @return Redis에 연결하기 위한 RedisConnectionFactory 객체
   */
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(new RedisStandaloneConfiguration());
  }

  /**
   * Redis 작업을 위한 RedisTemplate 빈을 생성
   *
   * @return Redis 작업을 위한 RedisTemplate 객체
   */
  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    return template;
  }

  /**
   * 메시지 처리를 위한 MessageListenerAdapter 빈을 생성
   *
   * @return 메시지 처리를 위한 MessageListenerAdapter 객체
   */
  @Bean
  public MessageListenerAdapter messageListenerAdapter() {
    return new MessageListenerAdapter(new MessageSubscribeService());
  }

  /**
   * Redis 메시지를 수신하기 위한 RedisMessageListenerContainer 빈을 생성
   *
   * @return Redis 메시지를 수신하기 위한 RedisMessageListenerContainer 객체
   */
  @Bean
  public RedisMessageListenerContainer redisContainer() {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory());
    container.addMessageListener(messageListenerAdapter(), channelTopic());
    return container;
  }
}
