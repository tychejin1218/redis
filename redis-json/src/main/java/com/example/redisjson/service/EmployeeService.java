package com.example.redisjson.service;

import com.example.redisjson.component.JedisComponent;
import com.example.redisjson.dto.EmployeeDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** *
 * EmployeeDto 객체를 Redis에 저장 및 조회
 *
 * <p>
 * Storing and Querying JSON documents using Redis Stack
 * https://developer.redis.com/howtos/redisjson/getting-started
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EmployeeService {

  private final JedisComponent jedisComponent;

  /**
   * EmployeeDto.Info를 Redis에 JSON 형식으로 저장
   *
   * @param key          Redis 키
   * @param employeeInfo EmployeeDto.Info
   * @return EmployeeDto.Info
   */
  public EmployeeDto.Info saveEmployeeInfo(String key, EmployeeDto.Info employeeInfo) {
    jedisComponent.setJson(key, employeeInfo, 60 * 5);
    return jedisComponent.getJsonObject(key, EmployeeDto.Info.class);
  }

  /**
   * EmployeeDto를 Redis에 JSON 형식으로 저장
   *
   * @param key      Redis 키
   * @param employee EmployeeDto
   * @return EmployeeDto
   */
  public EmployeeDto saveEmployee(String key, EmployeeDto employee) {
    jedisComponent.setJson(key, employee, 60 * 5);
    return jedisComponent.getJsonObject(key, EmployeeDto.class);
  }

  /**
   * 키(key)를 사용하여 Redis에 저장된 객체를 조회
   *
   * @param key Redis 키
   * @return EmployeeDto
   */
  public EmployeeDto findEmployee(String key) {
    return jedisComponent.getJsonObject(key, EmployeeDto.class);
  }

  /**
   * 키(key)와 경로(path)를 사용하여 Redis에 저장된 객체를 조회
   *
   * @param key  Redis 키
   * @param path 경로
   * @return List&lt;EmployeeDto.Info&lt;
   */
  public List<EmployeeDto.Info> findEmployee(String key, String path) throws Exception {
    return jedisComponent.getJsonArray(key, EmployeeDto.Info.class, path);
  }
}
