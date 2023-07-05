package com.example.redis.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "Employee", timeToLive = 60 * 5)
@Getter
@Setter
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
  @Setter
  @Builder
  public static class Department {

    public String department;
    private LocalDate transferDate;
  }
}
