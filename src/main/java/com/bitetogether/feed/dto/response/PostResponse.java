package com.bitetogether.feed.dto.response;

import com.bitetogether.feed.dto.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PostResponse extends BaseResponse {
  @Schema(description = "ID of the post", example = "68f5007b8082ca7e84af80e1")
  private String id;

  @Schema(description = "ID of the place associated with the post", example = "98765")
  private Long placeId;

  @Schema(description = "Content of the post", example = "Had a wonderful time at this restaurant!")
  private String content;

  @Schema(description = "Rating given in the post", example = "4")
  private Integer rating;

  @Schema(
      description = "URL of the photo associated with the post",
      example = "http://example.com/photo.jpg")
  private String photoUrl;

  @Schema(description = "Number of likes on the post", example = "100")
  private Integer likeCount;

  @Schema(description = "Number of comments on the post", example = "25")
  private Integer commentCount;

  @Schema(description = "Whether the current user has already liked this post", example = "true")
  private boolean alreadyLiked;

  @Schema(description = "User who created the post")
  private UserDTO user;
}
