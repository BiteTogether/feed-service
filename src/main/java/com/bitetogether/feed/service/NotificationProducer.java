package com.bitetogether.feed.service;

import com.bitetogether.feed.configuration.kafka.KafkaProperties;
import com.bitetogether.feed.dto.FeedNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka producer for feed notification events. Publishes LIKE, COMMENT, and NEARBY_CHECKIN events
 * to the feed-notification-events topic for the notification-service to consume, persist, and push.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final KafkaProperties kafkaProperties;

  /**
   * Publishes a feed notification event to Kafka. Uses receiverId as the partition key to ensure
   * ordering per recipient.
   */
  public void sendNotification(FeedNotificationEvent event) {
    String topic = kafkaProperties.getTopic().getFeedNotificationEvents();
    String key = event.getReceiverId().toString();

    try {
      kafkaTemplate
          .send(topic, key, event)
          .whenComplete(
              (result, ex) -> {
                if (ex != null) {
                  log.error(
                      "❌ Failed to send feed notification event to Kafka - Topic: {}, Type: {}, Receiver: {}, Error: {}",
                      topic,
                      event.getType(),
                      event.getReceiverId(),
                      ex.getMessage(),
                      ex);
                } else {
                  log.info(
                      "✅ Feed notification event sent - Topic: {}, Partition: {}, Offset: {}, Type: {}, Receiver: {}",
                      topic,
                      result.getRecordMetadata().partition(),
                      result.getRecordMetadata().offset(),
                      event.getType(),
                      event.getReceiverId());
                }
              });
    } catch (Exception e) {
      log.error(
          "❌ Kafka send failed for feed notification - Type: {}, Receiver: {}",
          event.getType(),
          event.getReceiverId(),
          e);
    }
  }
}
