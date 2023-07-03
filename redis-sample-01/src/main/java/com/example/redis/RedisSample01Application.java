package com.example.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableRedisRepositories
@SpringBootApplication
public class RedisSample01Application {

  public static void main(String[] args) {
    SpringApplication.run(RedisSample01Application.class, args);
  }

}
