package com.bitetogether.feed.dto.response;

import com.bitetogether.feed.dto.UserDTO;
import java.time.Instant;
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
