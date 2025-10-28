package com.bitetogether.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeRequest {
  @Schema(description = "ID of the post to be liked", example = "68f5007b8082ca7e84af80e1")
  private String postId;

  @Schema(description = "ID of the comment to be liked", example = "a1b2c3d4e5f6g7h8i9j0")
  private String commentId;
}
