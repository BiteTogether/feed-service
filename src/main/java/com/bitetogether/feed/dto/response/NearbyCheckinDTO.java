package com.bitetogether.feed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyCheckinDTO {
  @Schema(description = "ID of the user who checked in", example = "17")
  private Long userId;

  @Schema(description = "Full name of the user", example = "Tuyen Le")
  private String fullName;

  @Schema(description = "Avatar URL of the user", example = "http://example.com/avatar.jpg")
  private String avatar;

  @Schema(description = "ID of the nearby post", example = "69e9035087e1fc091d6ece5c")
  private String postId;

  @Schema(description = "Name of the place", example = "Pizza 4P's Lê Thánh Tôn")
  private String placeName;
}
