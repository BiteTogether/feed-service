package com.bitetogether.feed.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeRequest {
  private Long userId;
  private String postId;
  private String commentId;
}
