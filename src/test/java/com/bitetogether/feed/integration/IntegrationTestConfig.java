package com.bitetogether.feed.integration;

import com.bitetogether.feed.service.NotificationProducer;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@TestConfiguration
public class IntegrationTestConfig {

  @Bean
  @Primary
  @SuppressWarnings("unchecked")
  public ProducerFactory<String, Object> producerFactory() {
    return Mockito.mock(ProducerFactory.class);
  }

  @Bean
  @Primary
  @SuppressWarnings("unchecked")
  public KafkaTemplate<String, Object> kafkaTemplate() {
    return Mockito.mock(KafkaTemplate.class);
  }

  @Bean
  @Primary
  public NotificationProducer notificationProducer() {
    return Mockito.mock(NotificationProducer.class);
  }
}
