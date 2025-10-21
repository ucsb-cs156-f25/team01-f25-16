package edu.ucsb.cs156.example;

import edu.ucsb.cs156.example.services.wiremock.WiremockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** The ExampleApplication class is the main entry point for the application. */
@SpringBootApplication
@Slf4j
@EnableAsync // for @Async annotation for JobsService
@EnableScheduling // for @Scheduled annotation for JobsService
public class ExampleApplication {

  @Autowired WiremockService wiremockService;

  /**
   * When using the wiremock profile, this method will call the code needed to set up the wiremock
   * services
   */
  @Profile("wiremock")
  @Bean
  public ApplicationRunner wiremockApplicationRunner() {
    return arg -> {
      log.info("wiremock mode");
      wiremockService.init();
      log.info("wiremockApplicationRunner completed");
    };
  }

  /** Hook that can be used to set up any services needed for development */
  @Profile("development")
  @Bean
  public ApplicationRunner developmentApplicationRunner() {
    return arg -> {
      log.info("development mode");
      log.info("developmentApplicationRunner completed");
    };
  }

  /**
   * The main method is the entry point for the application.
   *
   * @param args command line arguments, typically unused for Spring Boot applications
   */
  public static void main(String[] args) {
    SpringApplication.run(ExampleApplication.class, args);
  }

  /**
   * Pull in git.properties from the classpath, if it exists. This is useful for displaying git
   * information in the application. We use this in the SystemInfoService. See:
   * https://www.baeldung.com/spring-git-information
   *
   * @return a propertySourcePlaceholderConfigurer for git.properties
   */
  @Bean
  public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
    PropertySourcesPlaceholderConfigurer propsConfig = new PropertySourcesPlaceholderConfigurer();
    propsConfig.setLocation(new ClassPathResource("git.properties"));
    propsConfig.setIgnoreResourceNotFound(true);
    propsConfig.setIgnoreUnresolvablePlaceholders(true);
    return propsConfig;
  }
}
