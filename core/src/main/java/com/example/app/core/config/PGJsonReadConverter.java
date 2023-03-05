package com.example.app.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@Slf4j
@ReadingConverter
@RequiredArgsConstructor
public class PGJsonReadConverter implements Converter<Json, Map<String, Object>> {

  private final ObjectMapper objectMapper;

  @Override
  public Map<String, Object> convert(Json source) {
    try {
      return objectMapper.readValue(source.asString(), Map.class);
    } catch (Exception e) {
      log.error("failed to parse json to value ", e);
    }
    return Collections.emptyMap();
  }
}
