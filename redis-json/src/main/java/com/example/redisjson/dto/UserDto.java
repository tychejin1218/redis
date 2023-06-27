package com.example.redisjson.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDto {

  private String name;
  private String email;
  private int age;
  private String city;
}
