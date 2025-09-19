package com.bitetogether.feed.dto;

import lombok.*;

@Data
public class FeedDTO {
  private Long id;
  private Long userId;
  private Long placeId;
  private String content;
  private Integer rating;
  private String photoUrl;
}
