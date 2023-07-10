package com.example.redis.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * EmployeeDto 객체를 Redis에 "EMPLOYEE"라는 이름의 해시로 저장하고, 해당 해시의 TTL(Time To Live)을 5분(60초 * 5)로 설정
 */
@RedisHash(value = "EMPLOYEE", timeToLive = 60 * 5)
@Getter
@Builder
public class EmployeeDto {

  @Id
  private String id;
  private String name;
  private String gender;
  private int age;
  private LocalDate hireDate;
  private List<Department> departments;

  @Getter
  @Builder
  public static class Department {

    public String department;
    private LocalDate transferDate;
  }
}
