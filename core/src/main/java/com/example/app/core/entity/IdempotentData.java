package com.example.app.core.entity;

import com.example.app.shared.helper.IdentifierGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "idempotent_data")
public class IdempotentData {

  @Id
  private String id = IdentifierGenerator.generateId();
  private String client;
  private String category;
  private String key;
  private String value;
  private long createdTime;
}
