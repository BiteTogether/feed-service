package com.bitetogether.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
  private Long actorId; // user thực hiện hành động
  private Long receiverId; // chủ sở hữu feed hoặc comment
  private String type; // "LIKE_FEED", "LIKE_COMMENT", "COMMENT_FEED"
  private String targetId; // id của feed hoặc comment
  private String message;
}
