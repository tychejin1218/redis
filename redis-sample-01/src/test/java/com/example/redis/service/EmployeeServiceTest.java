package com.example.redis.service;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.example.redis.dto.EmployeeDto;
import com.example.redis.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ObjectUtils;

@Slf4j
@SpringBootTest
class EmployeeServiceTest {

  @Autowired
  private EmployeeRepository employeeRepository;

  @Autowired
  private EmployeeService employeeService;

  @Autowired
  private ObjectMapper objectMapper;

  @DisplayName("saveEmployee_EmployeeDto를 Hash 형식으로 저장")
  @Test
  void testSaveEmployee() throws Exception {

    // Given
    List<EmployeeDto.Department> departments = new ArrayList<>();
    departments.add(
        EmployeeDto.Department.builder()
            .department("마케팅부")
            .transferDate(LocalDate.of(2019, 3, 11))
            .build());
    departments.add(
        EmployeeDto.Department.builder()
            .department("영업부")
            .transferDate(LocalDate.of(2021, 8, 16))
            .build());

    EmployeeDto requestEmployee = EmployeeDto.builder()
        .id("1001")
        .name("홍길동")
        .gender("남")
        .age(32)
        .hireDate(LocalDate.of(2019, 3, 11))
        .departments(departments)
        .build();

    // When
    EmployeeDto responseEmployee = employeeService.saveEmployee(requestEmployee);

    // Then
    log.debug("responseEmployee: {}", objectMapper.writeValueAsString(responseEmployee));
    assertFalse(ObjectUtils.isEmpty(responseEmployee));
  }

  @DisplayName("findByIdEmployee_EmployeeDto를 조회")
  @Test
  void testFindByIdEmployee() throws Exception {

    // Given
    List<EmployeeDto.Department> departments = new ArrayList<>();
    departments.add(
        EmployeeDto.Department.builder()
            .department("인사")
            .transferDate(LocalDate.of(2022, 5, 2))
            .build());

    EmployeeDto requestEmployee = EmployeeDto.builder()
        .id("1002")
        .name("이순신")
        .gender("남")
        .age(30)
        .hireDate(LocalDate.of(2022, 5, 2))
        .departments(departments)
        .build();

    String id = saveEmployee(requestEmployee);

    // When
    EmployeeDto responseEmployee = employeeService.findByIdEmployee(id);

    // Then
    log.debug("responseEmployeeDto: {}", objectMapper.writeValueAsString(responseEmployee));
    assertFalse(ObjectUtils.isEmpty(responseEmployee));
  }

  @Disabled
  String saveEmployee(EmployeeDto employeeDto) {
    return employeeRepository.save(employeeDto).getId();
  }
}
