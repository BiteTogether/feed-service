package com.bitetogether.feed.dto.response;

import com.bitetogether.feed.dto.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentResponse extends BaseResponse {
  @Schema(description = "ID of the comment", example = "a1b2c3d4e5f6g7h8i9j0")
  private String id;

  @Schema(description = "ID of the post being commented on", example = "68f5007b8082ca7e84af80e1")
  private String postId;

  @Schema(description = "Content of the comment", example = "This is a great post!")
  private String content;

  @Schema(description = "Number of likes on the comment", example = "42")
  private Integer likeCount;

  @Schema(description = "Whether the current user has already liked this comment", example = "true")
  private boolean alreadyLiked;

  @Schema(description = "User who made the comment")
  private UserDTO user;
}
