package com.example.redisjson.service;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.example.redisjson.dto.EmployeeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ObjectUtils;

@Slf4j
@SpringBootTest
class EmployeeServiceTest {

  @Autowired
  private EmployeeService employeeService;

  @Autowired
  private ObjectMapper objectMapper;

  @DisplayName("saveEmployee_Redis에 객체(EmployeeDto.Info)를 JSON 형식으로 저장")
  @Test
  void testSaveEmployeeObject() throws Exception {

    // Given
    String key = "employee_profile";
    EmployeeDto.Info employeeInfo = EmployeeDto.Info.builder()
        .name("Paul John")
        .email("paul.john@example.com")
        .age(42)
        .build();

    // When
    EmployeeDto.Info responseEmployeeInfo = employeeService.saveEmployeeInfo(key, employeeInfo);

    // Then
    log.debug("responseEmployeeInfo: {}", objectMapper.writeValueAsString(responseEmployeeInfo));
    assertFalse(ObjectUtils.isEmpty(responseEmployeeInfo));
  }

  @DisplayName("saveEmployee_Redis에 객체(EmployeeDto)를 JSON 형식으로 저장")
  @Test
  void testSaveEmployeeArray() throws Exception {

    // Given
    String key = "employee_info";
    List<EmployeeDto.Info> employees = new ArrayList<>();
    employees.add(EmployeeDto.Info.builder()
        .name("Alpha")
        .email("alpha@gmail.com")
        .age(23)
        .build());
    employees.add(EmployeeDto.Info.builder()
        .name("Beta")
        .email("beta@gmail.com")
        .age(28)
        .build());
    employees.add(EmployeeDto.Info.builder()
        .name("Gamma")
        .email("gamma@gmail.com")
        .age(33)
        .build());
    employees.add(EmployeeDto.Info.builder()
        .name("Theta")
        .email("theta@gmail.com")
        .age(41)
        .build());

    EmployeeDto employee = EmployeeDto.builder()
        .employees(employees)
        .build();

    // When
    EmployeeDto responseEmployee = employeeService.saveEmployee(key, employee);

    // Then
    log.debug("responseEmployee: {}", objectMapper.writeValueAsString(responseEmployee));
    assertFalse(ObjectUtils.isEmpty(responseEmployee));
  }

  @DisplayName("findEmployee_Redis 조회 시 EmployeeDto로 반환")
  @Test
  void testFindEmployee() throws Exception {

    // Given
    String key = "employee_info";

    // When
    EmployeeDto responseEmployee = employeeService.findEmployee(key);

    // Then
    log.debug("responseEmployee: {}", objectMapper.writeValueAsString(responseEmployee));
    assertFalse(ObjectUtils.isEmpty(responseEmployee));
  }

  @DisplayName("findEmployee_Redis 조회 시 EmployeeDto로 반환")
  @Test
  void testFindEmployeePath() throws Exception {

    // Given
    String key = "employee_info";
    String path = "$.employees[?(@.age>20&&@.age<40)]";

    // When
    List<EmployeeDto.Info> responseEmployee = employeeService.findEmployee(key, path);

    // Then
    log.debug("responseEmployee: {}", objectMapper.writeValueAsString(responseEmployee));
    assertFalse(ObjectUtils.isEmpty(responseEmployee));
  }
}
