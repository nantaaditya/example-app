package com.example.app.shared.configuration;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
public class DBInitialization {

  public ConnectionFactoryInitializer toConnectionFactoryInitializer(ConnectionFactory factory) {
    ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
    initializer.setConnectionFactory(factory);
    ResourceDatabasePopulator resource = new ResourceDatabasePopulator(
        new ClassPathResource("schema.sql"));
    initializer.setDatabasePopulator(resource);
    return initializer;
  }
}
