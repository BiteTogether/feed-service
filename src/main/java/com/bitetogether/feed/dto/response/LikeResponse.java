package com.bitetogether.feed.dto.response;

import java.time.Instant;

import com.bitetogether.feed.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeResponse {
  private String id;
  private String postId;
  private String commentId;
  private Instant createdAt;
  private UserDTO user;
}
