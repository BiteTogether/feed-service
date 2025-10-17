package com.bitetogether.feed.service;

import com.bitetogether.feed.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {

  private static final String TOPIC = "notification-events";

  private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

  public void sendNotification(NotificationEvent event) {
    try {
      kafkaTemplate
          .send(TOPIC, event.getReceiverId().toString(), event)
          .whenComplete(
              (result, ex) -> {
                if (ex != null) {
                  log.error("Failed to send notification event: {}", event, ex);
                } else {
                  log.info(
                      "Notification event sent to topic [{}] with offset={}",
                      TOPIC,
                      result.getRecordMetadata().offset());
                }
              });
    } catch (Exception e) {
      log.error("Kafka send failed for event: {}", event, e);
    }
  }
}
