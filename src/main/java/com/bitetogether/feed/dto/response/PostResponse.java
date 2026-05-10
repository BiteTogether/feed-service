package com.bitetogether.feed.dto.response;

import com.bitetogether.feed.dto.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class PostResponse extends BaseResponse {
  @Schema(description = "ID of the post", example = "68f5007b8082ca7e84af80e1")
  private String id;

  @Schema(description = "ID of the place associated with the post", example = "98765")
  private String placeId;

  @Schema(description = "Name of the place associated with the post", example = "Pizza 4P's")
  private String placeName;

  @Schema(
      description = "Address of the place associated with the post",
      example = "151B Hai Ba Trung, District 3, Ho Chi Minh City")
  private String placeAddress;

  @Schema(description = "Latitude of the post location", example = "10.7769")
  private Double latitude;

  @Schema(description = "Longitude of the post location", example = "106.7009")
  private Double longitude;

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

  @Schema(description = "Whether the current user has already saved this post", example = "true")
  private boolean alreadySaved;

  @Schema(description = "User who created the post")
  private UserDTO user;

  @Schema(description = "Nearby checkins from friends within 5-10m range")
  private List<NearbyCheckinDTO> nearbyCheckins;
}
