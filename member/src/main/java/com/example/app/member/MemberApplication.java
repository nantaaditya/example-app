package com.example.app.member;

import com.example.app.shared.configuration.DBInitialization;
import com.nantaaditya.framework.kafka.api.KafkaOutboxController;
import com.nantaaditya.framework.rest.api.CacheController;
import com.nantaaditya.framework.rest.handler.RestExceptionHandler;
import io.r2dbc.spi.ConnectionFactory;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableR2dbcAuditing
@EnableR2dbcRepositories(basePackages = "com.example.app.member.*")
@Import({RestExceptionHandler.class, CacheController.class, KafkaOutboxController.class})
@ComponentScan("com.example.app.*")
@OpenAPIDefinition(info =
  @Info(
      title = "member api doc",
      contact = @Contact(name = "Nanta Aditya", url = "https://nantaaditya.com")
  )
)
public class MemberApplication {


  public static void main(String[] args) {
    SpringApplication.run(MemberApplication.class, args);
  }

  @Bean
  public ConnectionFactoryInitializer initializer(
      DBInitialization dbInitialization, ConnectionFactory connectionFactory) {
    return dbInitialization.toConnectionFactoryInitializer(connectionFactory);
  }

  @Bean
  public ReactiveAuditorAware<String> auditorAware() {
    return () -> Mono.just("SYSTEM");
  }


}
