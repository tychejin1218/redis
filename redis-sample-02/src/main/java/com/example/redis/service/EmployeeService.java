package com.example.redis.service;

import com.example.redis.dto.EmployeeDto;
import com.example.redis.repository.EmployeeRepository;
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

  public EmployeeDto saveEmployee(EmployeeDto employeeDto) {
    return employeeRepository.save(employeeDto);
  }

  public EmployeeDto findByIdEmployee(String id) {
    return employeeRepository.findById(id).orElseThrow(() -> new NoSuchElementException());
  }

  public void deleteByIdEmployee(String id) {
    employeeRepository.deleteById(id);
  }

  public void findAllEmployee() {
    Iterable<EmployeeDto> employees  = employeeRepository.findAll();
    log.debug("employees: {}", employees);
  }

  public void findAllByIdEmployee(List<String> ids) {
    Iterable<EmployeeDto> employees  = employeeRepository.findAllById(ids);
    log.debug("employees: {}", employees);
  }
}
