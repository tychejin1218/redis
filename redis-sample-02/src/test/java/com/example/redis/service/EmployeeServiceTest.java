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
  void testFindAllEmployee() {

    // Given
    saveAllEmployees();

    // When
    employeeService.findAllEmployee();

    // Then
  }

  @DisplayName("findAllByIdEmployee_ID를 기준으로 EmployeeDto 전체를 조회")
  @Test
  void testFindAllByIdEmployee() {

    // Given
    saveAllEmployees();

    List<String> ids = Arrays.asList("E0001", "E0003", "E0005");

    // When
    employeeService.findAllByIdEmployee(ids);

    // Then
  }

  @Disabled
  String saveEmployee(EmployeeDto employeeDto) {
    return employeeRepository.save(employeeDto).getId();
  }

  @Disabled
  void saveAllEmployees() {

    List<EmployeeDto> employees = new ArrayList<>();

    employees.add(
        EmployeeDto.builder()
            .id("E0001")
            .name("세종대왕")
            .gender("남성")
            .age(30)
            .hireDate(LocalDate.of(2022, 1, 15))
            .departments(createSampleDepartments(LocalDate.of(2022, 1, 15), "마케팅", "영업"))
            .build());

    employees.add(
        EmployeeDto.builder()
            .id("E0002")
            .name("유관순")
            .gender("여성")
            .age(35)
            .hireDate(LocalDate.of(2021, 7, 10))
            .departments(createSampleDepartments(LocalDate.of(2021, 7, 10), "연구 및 개발"))
            .build());

    employees.add(
        EmployeeDto.builder()
            .id("E0003")
            .name("안중근")
            .gender("남성")
            .age(28)
            .hireDate(LocalDate.of(2022, 8, 29))
            .departments(createSampleDepartments(LocalDate.of(2023, 3, 1), "인사"))
            .build());

    employees.add(
        EmployeeDto.builder()
            .id("E0004")
            .name("신사임당")
            .gender("여성")
            .age(32)
            .hireDate(LocalDate.of(2020, 9, 8))
            .departments(createSampleDepartments(LocalDate.of(2020, 9, 8), "영업", "마케팅"))
            .build());

    employees.add(
        EmployeeDto.builder()
            .id("E0005")
            .name("윤동주")
            .gender("남성")
            .age(29)
            .hireDate(LocalDate.of(2022, 5, 12))
            .departments(createSampleDepartments(LocalDate.of(2022, 5, 12), "연구 및 개발"))
            .build());

    employeeRepository.saveAll(employees);
  }

  List<EmployeeDto.Department> createSampleDepartments(
      LocalDate localDate,
      String... departmentNames) {

    List<EmployeeDto.Department> departments = new ArrayList<>();
    LocalDate transferDate = localDate;

    for (String departmentName : departmentNames) {
      departments.add(
          EmployeeDto.Department.builder()
              .department(departmentName)
              .transferDate(transferDate)
              .build());

      transferDate = transferDate.plusMonths(6);
    }

    return departments;
  }
}
