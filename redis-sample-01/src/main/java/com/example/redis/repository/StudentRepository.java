package com.example.redis.repository;

import com.example.redis.dto.StudentDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends CrudRepository<StudentDto, String> {}
