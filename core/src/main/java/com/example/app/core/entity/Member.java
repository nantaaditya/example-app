package com.example.app.core.entity;

import com.example.app.shared.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "members")
public class Member extends BaseEntity {

  private String email;

  private String phoneNumber;

  private String name;
}
