package com.example.app.shared.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public class BaseResponse {
  private String id;

  private String createdBy;

  private long createdTime;

  private String modifiedBy;

  private long modifiedTime;

  private long version;
}
