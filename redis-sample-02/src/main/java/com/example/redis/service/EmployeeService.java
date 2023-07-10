package com.example.redis.service;

import com.example.redis.dto.EmployeeDto;
import com.example.redis.repository.EmployeeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmployeeService {

  private final EmployeeRepository employeeRepository;

  /**
   * EmployeeDto를 저장
   *
   * @param employeeDto 설명
   * @return 설명
   */
  public EmployeeDto saveEmployee(EmployeeDto employeeDto) {
    return employeeRepository.save(employeeDto);
  }

  public EmployeeDto findByIdEmployee(String id) {
    return employeeRepository.findById(id).orElseThrow(() -> new NoSuchElementException());
  }

  public void deleteByIdEmployee(String id) {
    employeeRepository.deleteById(id);
  }

  public List<EmployeeDto> findAllEmployee() {
    return employeeRepository.findAll();
  }

  public List<EmployeeDto> findAllByIdEmployee(List<String> ids) {
    return employeeRepository.findAllById(ids);
  }
}
