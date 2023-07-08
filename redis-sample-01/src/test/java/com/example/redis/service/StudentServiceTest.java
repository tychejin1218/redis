package com.example.redis.service;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.example.redis.dto.StudentDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ObjectUtils;

@Slf4j
@SpringBootTest
class StudentServiceTest {

  @Autowired
  private StudentService studentService;

  @Autowired
  private ObjectMapper objectMapper;

  @DisplayName("saveStudent_StudentDto를 HASH 형식으로 저장")
  @Test
  void testSaveStudent() throws Exception {

    // Given
    StudentDto studentDto = StudentDto.builder()
        .id("Eng2015001")
        .name("John Doe")
        .gender(StudentDto.Gender.MALE)
        .grade(1)
        .build();

    // When
    StudentDto responseStudent =  studentService.saveStudent(studentDto);

    // Then
    log.debug("responseStudent: {}", objectMapper.writeValueAsString(responseStudent));
    assertFalse(ObjectUtils.isEmpty(responseStudent));
  }
}
