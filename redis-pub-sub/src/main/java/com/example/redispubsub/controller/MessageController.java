package com.example.redispubsub.controller;

import com.example.redispubsub.dao.MessageDto;
import com.example.redispubsub.service.MessagePublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MessageController {

  private final MessagePublishService messagePublishService;

  /**
   * 메시지를 발행
   *
   * @param messageDto 발행할 메시지를 담은 MessageDto 객체
   * @return ResponseEntity 객체로 응답을 반환
   */
  @PostMapping("/message/publish")
  public ResponseEntity<?> messagePublish(@RequestBody MessageDto messageDto) {
    messagePublishService.publish(messageDto);
    return ResponseEntity.ok("Message sent to Redis!");
  }
}
