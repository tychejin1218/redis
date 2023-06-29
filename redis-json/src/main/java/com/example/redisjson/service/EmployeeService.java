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
   * @param employeeInfo 저장할 객체 (EmployeeDto.Info)
   * @return 저장된 객체 (EmployeeDto.Info)
   */
  public EmployeeDto.Info saveEmployeeInfo(String key, EmployeeDto.Info employeeInfo) {
    jedisComponent.setJson(key, employeeInfo);
    return jedisComponent.getJsonObject(key, EmployeeDto.Info.class);
  }

  /**
   * EmployeeDto를 Redis에 JSON 형식으로 저장
   *
   * @param key      Redis 키
   * @param employee 저장할 객체 (EmployeeDto)
   * @return 저장된 객체 (EmployeeDto)
   */
  public EmployeeDto saveEmployee(String key, EmployeeDto employee) {
    jedisComponent.setJson(key, employee);
    return jedisComponent.getJsonObject(key, EmployeeDto.class);
  }

  /**
   * 키(key)를 사용하여 Redis에 저장된 객체를 조회
   *
   * @param key Redis 키
   * @return 경로에 해당하는 객체 (Object)
   */
  public EmployeeDto findEmployee(String key) {
    return jedisComponent.getJsonObject(key, EmployeeDto.class);
  }

  /**
   * 키(key)와 경로(path)를 사용하여 Redis에 저장된 객체를 조회
   *
   * @param key  Redis 키
   * @param path 경로
   * @return 경로에 해당하는 객체 (List<Object>)
   */
  public List<EmployeeDto.Info> findEmployee(String key, String path) throws Exception {
    return jedisComponent.getJsonArray(key, EmployeeDto.Info.class, path);
  }
}
