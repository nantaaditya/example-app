package com.example.app.shared.model.kafka;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateBalanceEvent {
  private String id;

  private String createdBy;

  private long createdTime;

  private String modifiedBy;

  private long modifiedTime;

  private long version;

  private String memberId;

  private long amount;

  private String type;
}
