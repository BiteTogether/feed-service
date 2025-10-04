package com.bitetogether.feed.configuration.mongodb;

import static com.bitetogether.common.util.SecurityUtils.getCurrentUserId;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

@Slf4j
public class MongoAuditorAwareImpl implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    try {
      Long currentUserId = getCurrentUserId();

      if (currentUserId != null) {
        String auditor = "USER_" + currentUserId;
        return Optional.of(auditor);
      }

      return Optional.of("SYSTEM");

    } catch (Exception e) {
      return Optional.of("SYSTEM");
    }
  }
}

@Configuration
class MongoAuditorConfig {
  @Bean(name = "mongoAuditorProvider")
  public AuditorAware<String> feedAuditorProvider() {
    return new MongoAuditorAwareImpl();
  }
}
