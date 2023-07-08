package com.example.redis.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "Student", timeToLive = 30)
@Getter
@Setter
@Builder
public class StudentDto {

  public enum Gender {
    MALE, FEMALE
  }

  @Id
  private String id;
  private String name;
  private Gender gender;
  private int grade;
}
