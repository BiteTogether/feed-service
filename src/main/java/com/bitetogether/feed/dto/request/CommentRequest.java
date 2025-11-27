package com.bitetogether.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CommentRequest {
  @Schema(description = "ID of the post being commented on", example = "68f5007b8082ca7e84af80e1")
  private String postId;

  @Schema(description = "Content of the comment", example = "This is a great post!")
  private String content;

  @Schema(description = "ID of the parent comment (null for top-level comments)", example = "68f5007b8082ca7e84af80e2")
  private String parentCommentId;
}
