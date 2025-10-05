package com.bitetogether.feed.dto.request;

import lombok.Data;

@Data
public class FeedRequest {
  private Long userId;
  private Long placeId;
  private String content;
  private Integer rating;
  private String photoUrl;
}
