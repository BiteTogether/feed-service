package com.bitetogether.feed.dto.request;

import lombok.Data;

@Data
public class PostRequest {
  private Long userId;
  private Long placeId;
  private String content;
  private Integer rating;
  private String photoUrl;
}
