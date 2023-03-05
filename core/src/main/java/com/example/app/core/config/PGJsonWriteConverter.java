package com.example.app.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@Slf4j
@WritingConverter
@RequiredArgsConstructor
public class PGJsonWriteConverter implements Converter<Map<String, Object>, Json> {

  private final ObjectMapper objectMapper;

  @Override
  public Json convert(Map<String, Object> source) {
    if (source == null || source.isEmpty()) return null;
    try {
      return Json.of(objectMapper.writeValueAsString(source));
    } catch (Exception e) {
      log.error("failed to parse value to json ", e);
    }
    return Json.of(StringUtils.EMPTY);
  }
}
