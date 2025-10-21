package com.bitetogether.feed.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendDTO {
  @Schema(description = "ID of the friend", example = "12345")
  Long id;

  @Schema(description = "Username of the friend", example = "john_doe")
  String username;

  @Schema(description = "Full name of the friend", example = "John Doe")
  String fullName;

  @Schema(description = "Avatar URL of the friend", example = "http://example.com/avatar.jpg")
  String avatar;
}
