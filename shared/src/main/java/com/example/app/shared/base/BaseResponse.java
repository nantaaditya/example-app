package com.example.app.shared.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BaseResponse {
  private String id;

  private String createdBy;

  private long createdTime;

  private String modifiedBy;

  private long modifiedTime;

  private long version;
}
