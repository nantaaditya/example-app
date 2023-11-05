package com.example.app.shared.model.kafka;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateMemberEvent {
  private String id;

  private String createdBy;

  private long createdTime;

  private String modifiedBy;

  private long modifiedTime;

  private long version;

  private String email;

  private String phoneNumber;

  private String name;
}
