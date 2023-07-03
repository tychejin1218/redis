package com.example.redis.service;

import com.example.redis.dto.StudentDto;
import com.example.redis.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class StudentService {

  private final StudentRepository studentRepository;

  public StudentDto saveStudent(StudentDto studentDto) {
    return studentRepository.save(studentDto);
  }
}
