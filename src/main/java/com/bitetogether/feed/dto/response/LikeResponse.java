package com.bitetogether.feed.dto.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeResponse {
  private String id;
  private Long userId;
  private String postId;
  private String commentId;
  private Instant createdAt;
}
