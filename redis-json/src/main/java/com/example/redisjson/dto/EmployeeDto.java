package com.example.redisjson.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmployeeDto {

  List<Info> employees;

  @Getter
  @Setter
  @Builder
  public static class Info {

    private String name;
    private String email;
    private int age;
  }
}
