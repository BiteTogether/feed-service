package com.bitetogether.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
  private Long actorId;
  private Long receiverId;
  private String type;
  private String targetId;
  private String message;
}
