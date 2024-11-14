package com.example.app.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.spi.ConnectionFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;

@Configuration
@RequiredArgsConstructor
public class PGDatabaseConfiguration extends AbstractR2dbcConfiguration {

  private final ObjectMapper objectMapper;

  @Override
  public ConnectionFactory connectionFactory() {
    return null;
  }

  @Override
  public R2dbcCustomConversions r2dbcCustomConversions() {
    List<Converter<?, ?>> converters = new ArrayList<>();
    converters.add(new PGJsonWriteConverter(objectMapper));
    converters.add(new PGJsonReadConverter(objectMapper));
    return new R2dbcCustomConversions(getStoreConversions(), converters);
  }
}
