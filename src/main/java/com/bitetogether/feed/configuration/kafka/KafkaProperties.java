package com.bitetogether.feed.configuration.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaProperties {
  private String bootstrapServers;
  private Producer producer;
  private Topic topic;
  private Properties properties;

  @Data
  public static class Producer {
    private String keySerializer;
    private String valueSerializer;
  }

  @Data
  public static class Topic {
    private String feedNotificationEvents;
  }

  @Data
  public static class Properties {
    private String securityProtocol;
    private Ssl ssl;

    @Data
    public static class Ssl {
      private Keystore keystore;
      private Truststore truststore;
      private String endpointIdentificationAlgorithm;

      @Data
      public static class Keystore {
        private String type;
        private String location;
        private String password;
      }

      @Data
      public static class Truststore {
        private String type;
        private String location;
        private String password;
      }
    }
  }
}
