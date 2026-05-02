package com.bitetogether.feed.configuration.kafka;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

  private final KafkaProperties kafkaProperties;
  private final ResourceLoader resourceLoader;

  @Bean
  public ProducerFactory<String, Object> producerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    KafkaProperties.Properties kafkaProps = kafkaProperties.getProperties();
    if (kafkaProps != null && kafkaProps.getSecurityProtocol() != null) {
      props.put("security.protocol", kafkaProps.getSecurityProtocol());

      KafkaProperties.Properties.Ssl ssl = kafkaProps.getSsl();
      if (ssl != null) {
        configureSSL(props, ssl);
      }
    }

    return new DefaultKafkaProducerFactory<>(props);
  }

  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate(
      ProducerFactory<String, Object> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }

  private void configureSSL(Map<String, Object> props, KafkaProperties.Properties.Ssl ssl) {
    try {
      if (ssl.getKeystore() != null) {
        if (ssl.getKeystore().getType() != null)
          props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, ssl.getKeystore().getType());
        if (ssl.getKeystore().getLocation() != null) {
          Resource r = resourceLoader.getResource(ssl.getKeystore().getLocation());
          props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, r.getFile().getAbsolutePath());
        }
        if (ssl.getKeystore().getPassword() != null)
          props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, ssl.getKeystore().getPassword());
      }
      if (ssl.getTruststore() != null) {
        if (ssl.getTruststore().getType() != null)
          props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, ssl.getTruststore().getType());
        if (ssl.getTruststore().getLocation() != null) {
          Resource r = resourceLoader.getResource(ssl.getTruststore().getLocation());
          props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, r.getFile().getAbsolutePath());
        }
        if (ssl.getTruststore().getPassword() != null)
          props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, ssl.getTruststore().getPassword());
      }
      if (ssl.getEndpointIdentificationAlgorithm() != null)
        props.put(
            SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG,
            ssl.getEndpointIdentificationAlgorithm());
    } catch (IOException e) {
      log.error("Failed to load SSL keystore/truststore files", e);
      throw new RuntimeException("Failed to load SSL keystore/truststore files", e);
    }
  }
}
