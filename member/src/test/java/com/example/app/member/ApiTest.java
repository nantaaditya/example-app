package com.example.app.member;

import com.nantaaditya.framework.test.service.RestApiIntegrationService;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
    classes = MemberApplication.class,
    webEnvironment = WebEnvironment.DEFINED_PORT
)
public class ApiTest {

  @Autowired
  private RestApiIntegrationService restApiIntegrationService;

  @TestFactory
  public Stream<DynamicTest> member() {
    return restApiIntegrationService.runGroupAPITest("member");
  }

  @Test
  public void single() {
    restApiIntegrationService.runSingleAPITest("member", "get_member");
  }
}
