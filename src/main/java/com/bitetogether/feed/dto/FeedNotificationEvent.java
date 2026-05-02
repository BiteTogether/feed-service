package com.bitetogether.feed.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedNotificationEvent {

  /** User ID of the person performing the action (liker, commenter, poster) */
  private Long actorId;

  /** User ID of the person receiving the notification (post owner) */
  private Long receiverId;

  /** Notification type: LIKE, COMMENT, NEARBY_CHECKIN */
  private String type;

  /** Target entity ID (postId for LIKE/COMMENT/NEARBY_CHECKIN) */
  private String targetId;

  /** Notification title (e.g., actor's name) */
  private String title;

  /** Notification message body */
  private String message;

  /** Actor's display name */
  private String actorName;

  /** Actor's avatar URL */
  private String actorAvatar;

  /** Source service identifier */
  @Builder.Default private String sourceService = "feed-service";

  /** Timestamp when the event was created */
  @Builder.Default private LocalDateTime timestamp = LocalDateTime.now();
}
