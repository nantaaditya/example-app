package com.example.app.core.entity;

import com.example.app.shared.base.BaseEntity;
import com.example.app.shared.model.kafka.CreateMemberEvent;
import com.nantaaditya.framework.helper.converter.ConverterHelper;
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

  public static Member from(CreateMemberEvent createMemberEvent) {
    Member member = ConverterHelper.copy(createMemberEvent, Member::new);
    member.setVersion(0);
    return member;
  }
}
