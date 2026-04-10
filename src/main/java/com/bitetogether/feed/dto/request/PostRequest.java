package com.bitetogether.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PostRequest {
  @Schema(description = "ID of the place associated with the post", example = "67890")
  private Long placeId;

  @Schema(description = "Latitude of the post location", example = "10.7769")
  private Double latitude;

  @Schema(description = "Longitude of the post location", example = "106.7009")
  private Double longitude;

  @Schema(description = "Content of the post", example = "Had an amazing meal at this restaurant!")
  private String content;

  @Schema(description = "Rating given in the post", example = "5")
  private Integer rating;

  @Schema(
      description = "URL of the photo associated with the post",
      example = "http://example.com/photo.jpg")
  private String photoUrl;
}
