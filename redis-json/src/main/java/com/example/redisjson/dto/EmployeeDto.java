package com.example.redisjson.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmployeeDto {

  List<Info> employees;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Info {

    private String name;
    private String email;
    private int age;
  }
}
