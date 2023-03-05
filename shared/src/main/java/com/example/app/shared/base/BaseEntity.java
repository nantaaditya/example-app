package com.example.app.shared.base;

import com.example.app.shared.helper.IdentifierGenerator;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

@Data
@NoArgsConstructor
@SuperBuilder
public class BaseEntity {
  @Id
  private String id = IdentifierGenerator.generateId();

  @CreatedBy
  private String createdBy;

  @CreatedDate
  private long createdTime;

  @LastModifiedBy
  private String modifiedBy;

  @LastModifiedDate
  private long modifiedTime;

  @Version
  private long version;
}
