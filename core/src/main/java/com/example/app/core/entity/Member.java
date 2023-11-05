package com.example.app.core.entity;

import com.example.app.shared.base.BaseEntity;
import com.example.app.shared.model.kafka.CreateMemberEvent;
import com.nantaaditya.framework.helper.json.JsonHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;
import reactor.kafka.receiver.ReceiverRecord;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "members")
public class Member extends BaseEntity {

  private String email;

  private String phoneNumber;

  private String name;

  public static Member from(ReceiverRecord<String, String> memberEvent, JsonHelper jsonHelper) {
    CreateMemberEvent event = jsonHelper.fromJson(memberEvent.value(), CreateMemberEvent.class);
    return Member.builder()
        .id(memberEvent.key())
        .email(event.getEmail())
        .phoneNumber(event.getPhoneNumber())
        .name(event.getName())
        .build();
  }
}
