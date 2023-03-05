package com.example.app.shared.helper;

import com.github.f4b6a3.tsid.TsidCreator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdentifierGenerator {

  public static String generateId() {
    return TsidCreator.getTsid256().toLowerCase();
  }
}
