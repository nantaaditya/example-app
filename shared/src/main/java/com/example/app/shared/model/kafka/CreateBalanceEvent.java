package com.example.app.shared.model.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.beans.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateBalanceEvent {
  private String id;

  private String createdBy;

  private long createdTime;

  private String modifiedBy;

  private long modifiedTime;

  private long version;

  private String memberId;

  private long amount;

  private String type;

  @Transient
  @JsonIgnore
  public String getUniqueId() {
    return this.id + "_" + this.type;
  }
}
