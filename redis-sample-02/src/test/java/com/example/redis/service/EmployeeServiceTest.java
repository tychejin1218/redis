package com.example.redis.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.redis.dto.EmployeeDto;
import com.example.redis.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
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

  @DisplayName("saveEmployee_EmployeeDto를 저장")
  @Test
  void testSaveEmployee() throws Exception {

    // Given
    List<EmployeeDto.Department> departments = new ArrayList<>();
    departments.add(
        EmployeeDto.Department.builder()
            .department("마케팅")
            .transferDate(LocalDate.of(2019, 3, 11))
            .build());
    departments.add(
        EmployeeDto.Department.builder()
            .department("영업")
            .transferDate(LocalDate.of(2021, 8, 16))
            .build());

    EmployeeDto requestEmployee = EmployeeDto.builder()
        .id("E0001")
        .name("세종대왕")
        .gender("남")
        .age(35)
        .hireDate(LocalDate.of(2019, 3, 11))
        .departments(departments)
        .build();

    // When
    EmployeeDto responseEmployee = employeeService.saveEmployee(requestEmployee);

    // Then
    log.debug("responseEmployee: {}", objectMapper.writeValueAsString(responseEmployee));
    assertFalse(ObjectUtils.isEmpty(responseEmployee));
  }

  @DisplayName("findByIdEmployee_ID를 기준으로 EmployeeDto를 조회")
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
        .id("E0002")
        .name("이순신")
        .gender("남")
        .age(45)
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

  @DisplayName("findByIdEmployee_ID를 기준으로 EmployeeDto를 삭제")
  @Test
  void deleteFindByIdEmployee() {

    // Given
    List<EmployeeDto.Department> departments = new ArrayList<>();
    departments.add(
        EmployeeDto.Department.builder()
            .department("개발")
            .transferDate(LocalDate.of(2023, 5, 1))
            .build());

    EmployeeDto requestEmployee = EmployeeDto.builder()
        .id("E0003")
        .name("장영실")
        .gender("남")
        .age(42)
        .hireDate(LocalDate.of(2023, 5, 1))
        .departments(departments)
        .build();

    String id = saveEmployee(requestEmployee);

    // When
    employeeService.deleteByIdEmployee(id);

    // Then
    assertThrows(NoSuchElementException.class, () -> employeeService.findByIdEmployee(id));
  }

  @DisplayName("findAllEmployee_EmployeeDto 전체를 조회")
  @Test
  void testFindAllEmployee() throws Exception {

    // Given
    saveEmployeeSample();

    // When
    employeeService.findAllEmployee();

    // Then
  }

  @DisplayName("findAllByIdEmployee_ID를 기준으로 EmployeeDto 전체를 조회")
  @Test
  void testFindAllByIdEmployee() throws Exception {

    // Given
    saveEmployeeSample();

    List<String> ids = Arrays.asList("E0001", "E0002", "E0003");

    // When
    employeeService.findAllByIdEmployee(ids);

    // Then
  }

  @Disabled
  String saveEmployee(EmployeeDto employeeDto) {
    return employeeRepository.save(employeeDto).getId();
  }

  @Disabled
  void saveEmployeeSample() {

    for (int a = 0; a < 10; a++) {

      List<EmployeeDto.Department> departments = new ArrayList<>();
      departments.add(
          EmployeeDto.Department.builder()
              .department("인사")
              .transferDate(LocalDate.of(2022, 5, 2))
              .build());

      EmployeeDto employee = EmployeeDto.builder()
          .id("E" + String.format("%04d", a))
          .name("name_" + a)
          .gender("남")
          .age(45)
          .hireDate(LocalDate.of(2022, 5, 2))
          .departments(departments)
          .build();

      employeeRepository.save(employee);
    }
  }
}
