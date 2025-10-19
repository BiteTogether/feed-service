package com.bitetogether.feed.dto.request;

import lombok.Data;

@Data
public class CommentRequest {
  private Long userId;
  private String postId;
  private String content;
}
