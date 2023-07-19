package com.example.redispubsub.dao;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Service;

@Getter
@Service
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto implements Serializable {

  private String sender;
  private String context;
}
