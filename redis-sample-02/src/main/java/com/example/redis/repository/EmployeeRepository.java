package com.example.redis.repository;

import com.example.redis.dto.EmployeeDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends CrudRepository<EmployeeDto, String> {}