package com.bitetogether.feed.dto.response;

import com.bitetogether.feed.dto.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeResponse extends BaseResponse {
  @Schema(description = "ID of the like", example = "d4e5f6g7h8i9j0a1b2c3")
  private String id;

  @Schema(
      description = "ID of the post that was liked or the post which the liked comment belongs",
      example = "68f5007b8082ca7e84af80e1")
  private String postId;

  @Schema(description = "ID of the comment that was liked", example = "a1b2c3d4e5f6g7h8i9j0")
  private String commentId;

  @Schema(description = "User who liked the post or comment")
  private UserDTO user;
}
