package com.bitetogether.feed.service;

import com.bitetogether.feed.configuration.kafka.KafkaProperties;
import com.bitetogether.feed.dto.FeedNotificationEvent;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class NotificationProducerTest {

  @Mock private KafkaTemplate<String, Object> kafkaTemplate;

  @Mock private KafkaProperties kafkaProperties;

  @InjectMocks private NotificationProducer notificationProducer;

  @Test
  void sendNotification_success_sendsToKafka() {
    KafkaProperties.Topic topic = new KafkaProperties.Topic();
    topic.setFeedNotificationEvents("feed-notification-events");
    Mockito.when(kafkaProperties.getTopic()).thenReturn(topic);

    CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
    Mockito.when(
            kafkaTemplate.send(
                Mockito.eq("feed-notification-events"), Mockito.eq("100"), Mockito.any()))
        .thenReturn(future);

    FeedNotificationEvent event =
        FeedNotificationEvent.builder()
            .actorId(1L)
            .receiverId(100L)
            .type("LIKE")
            .targetId("post1")
            .title("New Like")
            .message("Someone liked your post")
            .build();

    notificationProducer.sendNotification(event);

    Mockito.verify(kafkaTemplate)
        .send(Mockito.eq("feed-notification-events"), Mockito.eq("100"), Mockito.same(event));
  }

  @Test
  void sendNotification_whenKafkaThrows_doesNotPropagate() {
    KafkaProperties.Topic topic = new KafkaProperties.Topic();
    topic.setFeedNotificationEvents("feed-notification-events");
    Mockito.when(kafkaProperties.getTopic()).thenReturn(topic);

    Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
        .thenThrow(new RuntimeException("kafka down"));

    FeedNotificationEvent event =
        FeedNotificationEvent.builder()
            .actorId(1L)
            .receiverId(100L)
            .type("COMMENT")
            .targetId("post1")
            .title("New Comment")
            .message("Someone commented")
            .build();

    // Should not throw
    org.junit.jupiter.api.Assertions.assertDoesNotThrow(
        () -> notificationProducer.sendNotification(event));
  }
}
